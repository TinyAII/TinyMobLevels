package nl.tinyaii.tinymoblevels.listener;

import nl.tinyaii.tinymoblevels.TinyMobLevelsPlugin;
import nl.tinyaii.tinymoblevels.data.LevelConfig;
import nl.tinyaii.tinymoblevels.data.MobLevel;
import nl.tinyaii.tinymoblevels.util.EcoBridge;
import nl.tinyaii.tinymoblevels.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * 怪物等级核心：
 *  - 刷怪时按等级来源(随机/距离)给敌对怪分级 + 应用血量/装备 + 显示等级
 *  - 玩家攻击分级怪时按防御倍数减伤
 *  - 击杀分级怪掉落金币(进钱包)/经验(可选)/自定义掉落
 */
public class MobLevelListener implements Listener {

    private final TinyMobLevelsPlugin plugin;
    private final LevelConfig config;
    private final MobLevel mobLevel;
    private final EcoBridge eco;
    private final Random random = new Random();
    /** 可见性缓存：实体UUID → 上次是否可见（只有状态变化才更新名字，避免重复发包） */
    private final java.util.Map<java.util.UUID, Boolean> visibleCache = new java.util.concurrent.ConcurrentHashMap<>();

    public MobLevelListener(TinyMobLevelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getLevelConfig();
        this.mobLevel = plugin.getMobLevel();
        this.eco = plugin.getEcoBridge();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        if (!plugin.getConfig().getBoolean("settings.enabled", true)) return;
        LivingEntity entity = e.getEntity();
        if (entity instanceof Player) return;
        // 只给敌对怪分级
        boolean hostileOnly = plugin.getConfig().getBoolean("settings.hostile-only", true);
        if (hostileOnly && !(entity instanceof Monster)) return;
        if (mobLevel.isLeveled(entity)) return;   // 已分级（手动召唤的不重复）

        int level = calcLevel(entity.getLocation());
        if (level < 1) return;
        applyLevel(entity, level);
    }

    /** 计算等级：随机 或 离出生点距离 */
    private int calcLevel(Location loc) {
        String source = plugin.getConfig().getString("settings.level-source", "random");
        if ("distance".equalsIgnoreCase(source)) {
            // 每1000米+1级，10级=10000米外
            World w = loc.getWorld();
            if (w == null) return 1;
            Location spawn = w.getSpawnLocation();
            double dist = loc.distance(spawn);
            int level = (int) (dist / 1000) + 1;
            return Math.max(1, Math.min(10, level));
        }
        // 随机（权重偏低级）
        int r = random.nextInt(100);
        if (r < 20) return 1;
        if (r < 40) return 2;
        if (r < 58) return 3;
        if (r < 73) return 4;
        if (r < 84) return 5;
        if (r < 92) return 6;
        if (r < 97) return 7;
        if (r < 99) return 8;
        if (r < 100) return 9;
        return 10;
    }

    /** 应用等级：血量/装备/显示 */
    public void applyLevel(LivingEntity entity, int level) {
        double[] stats = config.stats(level);
        double defense = stats[2];

        // 血量（原版基础 × 倍数），封顶 1024；溢出部分折算成防御（不封顶）
        // 史莱姆/岩浆怪：MAX_HEALTH 属性由 size 决定，直接读属性即可（1.16+ 都有该属性）
        double baseHealth = entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = baseHealth * stats[0];
        if (newHealth > 1024.0) {
            double overflowRatio = newHealth / 1024.0;   // 期望血量 / 封顶值
            newHealth = 1024.0;
            defense = defense * overflowRatio;           // 溢出折进防御（不封顶）
        }
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        entity.setHealth(newHealth);
        // 标记等级 + 实际防御（减伤用）
        mobLevel.setLevel(entity, level, defense);

        // 攻击（原版基础 × 倍数）
        if (entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            double baseAtk = entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).getValue();
            entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(baseAtk * stats[1]);
        }

        // 装备（武器/护甲独立掷骰）
        equip(entity, level);

        // 头顶等级显示：等级 / 怪物名 / 血量，三项分开（血量实时刷新）
        if (plugin.getConfig().getBoolean("settings.level-display", true)) {
            entity.setCustomName(nameLine(entity, level));
            entity.setCustomNameVisible(true);
        }
    }

    /** 头顶显示行： [Lv.N]  僵尸  20/32（血量封顶显示 1024） */
    private String nameLine(LivingEntity entity, int level) {
        String name = nl.tinyaii.tinymoblevels.util.MonsterNames.name(entity.getType());
        int hp = (int) Math.ceil(entity.getHealth());
        int maxHp = (int) Math.ceil(entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
        return Messages.color("&c[Lv." + level + "]  &f" + name + "  &a" + hp + "&7/&a" + maxHp);
    }

    private void equip(LivingEntity entity, int level) {
        // 武器
        LevelConfig.WeaponConfig wc = config.weapon(level);
        if (wc != null && wc.material != null && random.nextDouble() < wc.chance) {
            ItemStack weapon = new ItemStack(wc.material);
            if (!wc.enchant.isEmpty()) {
                weapon.addUnsafeEnchantment(enchantByName(wc.enchant), wc.enchantLevel);
            }
            entity.getEquipment().setItemInMainHand(weapon);
        }
        // 护甲（随机1-4件）
        LevelConfig.ArmorConfig ac = config.armor(level);
        if (ac != null && !"NONE".equals(ac.material) && random.nextDouble() < ac.chance) {
            int pieces = 1 + random.nextInt(4);   // 1-4件
            applyArmorPieces(entity, ac, pieces);
        }
    }

    private void applyArmorPieces(LivingEntity entity, LevelConfig.ArmorConfig ac, int pieces) {
        // 头盔/胸甲/护腿/靴子
        String[][] map = armorMap(ac.material);
        int applied = 0;
        for (int i = 0; i < 4 && applied < pieces; i++) {
            if (random.nextBoolean() || applied == pieces - 1) {
                Material mat = Material.matchMaterial(map[i][0]);
                if (mat == null) continue;
                ItemStack armor = new ItemStack(mat);
                if (!ac.enchant.isEmpty()) {
                    armor.addUnsafeEnchantment(enchantByName(ac.enchant), ac.enchantLevel);
                }
                if (i == 0) entity.getEquipment().setHelmet(armor);
                else if (i == 1) entity.getEquipment().setChestplate(armor);
                else if (i == 2) entity.getEquipment().setLeggings(armor);
                else entity.getEquipment().setBoots(armor);
                applied++;
            }
        }
    }

    private String[][] armorMap(String material) {
        switch (material) {
            case "LEATHER": return new String[][]{{"LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"}, {"0","1","2","3"}};
            case "IRON": return new String[][]{{"IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS"}, {"0","1","2","3"}};
            case "CHAINMAIL_OR_GOLD": return new String[][]{{"CHAINMAIL_HELMET", "GOLDEN_CHESTPLATE", "CHAINMAIL_LEGGINGS", "GOLDEN_BOOTS"}, {"0","1","2","3"}};
            case "DIAMOND": return new String[][]{{"DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS"}, {"0","1","2","3"}};
            default: return new String[][]{{"LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"}, {"0","1","2","3"}};
        }
    }

    private Enchantment enchantByName(String name) {
        for (Enchantment e : Enchantment.values()) {
            if (e.getKey().getKey().equalsIgnoreCase(name) || e.getName().equalsIgnoreCase(name)) return e;
        }
        return null;
    }

    /**
     * 分级怪受击 → 刷新头顶血量（所有伤害来源，含火烧/岩浆/摔落/爆炸）。
     * 玩家攻击时额外按防御倍数减伤。
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity mob = (LivingEntity) e.getEntity();
        int level = mobLevel.getLevel(mob);
        if (level < 1) return;
        // 玩家攻击 → 减伤（用实际防御，含溢出折算）
        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent byEntity = (EntityDamageByEntityEvent) e;
            if (byEntity.getDamager() instanceof Player) {
                double defense = mobLevel.getDefense(mob);
                e.setDamage(e.getDamage() / defense);
            }
        }
        // 任何伤害都刷新头顶血量
        if (plugin.getConfig().getBoolean("settings.level-display", true)) {
            refreshDisplay(mob, level);
        }
    }

    /** 刷新头顶显示（血量 + 可选"玩家看不到怪就隐藏"防透视）。
     *  可见时总是更新名字（血量实时刷新）；隐藏状态只发一次（避免重复发包）。 */
    public void refreshDisplay(LivingEntity entity, int level) {
        boolean hideNoLos = plugin.getConfig().getBoolean("settings.level-hide-no-los", true);
        boolean visible = !(hideNoLos && !isSeenByAnyPlayer(entity));

        Boolean prev = visibleCache.get(entity.getUniqueId());
        if (!visible) {
            // 隐藏：仅在刚进入隐藏状态时关闭名字显示
            if (prev == null || prev) {
                entity.setCustomNameVisible(false);
            }
            visibleCache.put(entity.getUniqueId(), false);
            return;
        }
        // 可见：总是刷新名字（含实时血量），血量变了就会更新
        entity.setCustomName(nameLine(entity, level));
        entity.setCustomNameVisible(true);
        visibleCache.put(entity.getUniqueId(), true);
    }

    /** 是否至少有一个玩家能看到该怪（眼睛→怪眼睛，中间无不透明方块阻挡） */
    private boolean isSeenByAnyPlayer(LivingEntity entity) {
        org.bukkit.Location entityEye = entity.getEyeLocation();
        for (Player p : entity.getWorld().getPlayers()) {
            if (!p.isOnline()) continue;
            org.bukkit.Location playerEye = p.getEyeLocation();
            if (playerEye.getWorld() == null || !playerEye.getWorld().equals(entityEye.getWorld())) continue;
            // 简单距离预筛：太远（>32格）视为看不到（原版名字显示也有距离限制）
            if (playerEye.distanceSquared(entityEye) > 32 * 32) continue;
            // 视线检测：眼睛→怪眼睛，逐格采样检查不透明方块（跨版本兼容，不依赖 rayTraceBlocks 签名）
            if (hasLineOfSight(playerEye, entityEye)) return true;
        }
        return false;
    }

    /** 逐格采样检测两点间是否有不透明方块阻挡（跨版本，1.16~26.2 通用） */
    private boolean hasLineOfSight(org.bukkit.Location from, org.bukkit.Location to) {
        org.bukkit.World w = from.getWorld();
        if (w == null) return false;
        org.bukkit.util.Vector dir = to.clone().subtract(from).toVector();
        double dist = dir.length();
        if (dist <= 0) return true;
        org.bukkit.util.Vector step = dir.normalize().multiply(0.5);   // 每 0.5 格采样
        org.bukkit.Location cur = from.clone();
        for (double d = 0; d < dist; d += 0.5) {
            org.bukkit.block.Block block = w.getBlockAt(cur.getBlockX(), cur.getBlockY(), cur.getBlockZ());
            if (block.getType().isOccluding()) return false;   // 不透明方块挡住 → 看不到
            cur.add(step);
        }
        return true;
    }

    /** 击杀分级怪 → 金币/经验/掉落 */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        int level = mobLevel.getLevel(entity);
        if (level < 1) return;
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;

        // 金币（直接进钱包）
        if (plugin.getConfig().getBoolean("economy.enabled", true) && eco.isAvailable()) {
            int[] range = config.coins(level);
            int coins = range[0] + random.nextInt(range[1] - range[0] + 1);
            if (eco.deposit(killer, coins)) {
                String name = plugin.getConfig().getString("economy.coin-name", "金币");
                killer.sendMessage(Messages.color(plugin.getConfig().getString(
                        "messages.coin-earned", "&a击杀奖励 +{coins} {name}")
                        .replace("{coins}", String.valueOf(coins)).replace("{name}", name)));
            }
        }
        // 经验（可选）
        if (plugin.getConfig().getBoolean("exp.enabled", false)) {
            double mult = 1 + (level - 1) * plugin.getConfig().getDouble("exp.multiplier-per-level", 0.5);
            killer.giveExp((int) (e.getDroppedExp() * mult));
        }
        // 自定义掉落（config drops.等级.items）
        applyCustomDrops(entity, level);
    }

    private void applyCustomDrops(LivingEntity entity, int level) {
        org.bukkit.configuration.ConfigurationSection drops = plugin.getConfig()
                .getConfigurationSection("drops." + level + ".items");
        if (drops == null) return;
        for (String key : drops.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection item = drops.getConfigurationSection(key);
            if (item == null) continue;
            Material mat = Material.matchMaterial(item.getString("item", ""));
            if (mat == null) continue;
            int amount = item.getInt("amount", 1);
            double chance = item.getDouble("chance", 1.0);
            if (random.nextDouble() < chance) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(mat, amount));
            }
        }
    }
}
