package nl.tinyaii.tinymoblevels.data;

import nl.tinyaii.tinymoblevels.TinyMobLevelsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * 等级配置读取：数值表/装备/金币/经验，全部从 config 读。
 */
public class LevelConfig {

    private final TinyMobLevelsPlugin plugin;
    private final Map<Integer, double[]> stats = new HashMap<>();   // level -> [health, attack, defense]
    private final Map<Integer, WeaponConfig> weapons = new HashMap<>();
    private final Map<Integer, ArmorConfig> armors = new HashMap<>();
    private final Map<Integer, int[]> coins = new HashMap<>();      // level -> [min, max]

    /** 武器配置 */
    public static class WeaponConfig {
        public Material material;     // null=无武器
        public double chance;
        public String enchant;        // 附魔名（如 SHARPNESS），空=无
        public int enchantLevel;
    }

    /** 护甲配置 */
    public static class ArmorConfig {
        public String material;       // LEATHER/IRON/CHAINMAIL_OR_GOLD/DIAMOND
        public double chance;
        public String enchant;
        public int enchantLevel;
    }

    public LevelConfig(TinyMobLevelsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        stats.clear();
        weapons.clear();
        armors.clear();
        coins.clear();
        ConfigurationSection levels = plugin.getConfig().getConfigurationSection("levels");
        if (levels != null) {
            for (String key : levels.getKeys(false)) {
                int lv = Integer.parseInt(key);
                ConfigurationSection sec = levels.getConfigurationSection(key);
                stats.put(lv, new double[]{
                        sec.getDouble("health", 1), sec.getDouble("attack", 1), sec.getDouble("defense", 1)
                });
            }
        }
        ConfigurationSection eq = plugin.getConfig().getConfigurationSection("equipment");
        if (eq != null) {
            for (String key : eq.getKeys(false)) {
                int lv = Integer.parseInt(key);
                ConfigurationSection sec = eq.getConfigurationSection(key);
                ConfigurationSection w = sec.getConfigurationSection("weapon");
                if (w != null) {
                    WeaponConfig wc = new WeaponConfig();
                    String mat = w.getString("material", "NONE");
                    wc.material = "NONE".equalsIgnoreCase(mat) ? null : Material.matchMaterial(mat);
                    wc.chance = w.getDouble("chance", 0);
                    wc.enchant = w.getString("enchant", "");
                    wc.enchantLevel = w.getInt("level", 1);
                    weapons.put(lv, wc);
                }
                ConfigurationSection a = sec.getConfigurationSection("armor");
                if (a != null) {
                    ArmorConfig ac = new ArmorConfig();
                    ac.material = a.getString("material", "NONE");
                    ac.chance = a.getDouble("chance", 0);
                    ac.enchant = a.getString("enchant", "");
                    ac.enchantLevel = a.getInt("level", 1);
                    armors.put(lv, ac);
                }
            }
        }
        ConfigurationSection coinSec = plugin.getConfig().getConfigurationSection("economy.coins");
        if (coinSec != null) {
            for (String key : coinSec.getKeys(false)) {
                int lv = Integer.parseInt(key);
                ConfigurationSection sec = coinSec.getConfigurationSection(key);
                coins.put(lv, new int[]{sec.getInt("min", 1), sec.getInt("max", 2)});
            }
        }
    }

    /** 血量/攻击/防御 倍数（默认 1 级数值） */
    public double[] stats(int level) {
        double[] s = stats.get(Math.max(1, Math.min(10, level)));
        return s == null ? new double[]{1.6, 1.4, 1.2} : s;
    }

    public WeaponConfig weapon(int level) {
        return weapons.get(Math.max(1, Math.min(10, level)));
    }

    public ArmorConfig armor(int level) {
        return armors.get(Math.max(1, Math.min(10, level)));
    }

    /** 金币掉落 [min,max]，level 钳到 1-10 */
    public int[] coins(int level) {
        int[] c = coins.get(Math.max(1, Math.min(10, level)));
        return c == null ? new int[]{1, 2} : c;
    }
}
