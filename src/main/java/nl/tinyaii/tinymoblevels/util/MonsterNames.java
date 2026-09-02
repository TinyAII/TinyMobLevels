package nl.tinyaii.tinymoblevels.util;

import org.bukkit.entity.EntityType;

/**
 * 怪物中文名映射：头顶等级显示用（"僵尸" 而非 "Zombie"）。
 */
public final class MonsterNames {

    private MonsterNames() {}

    private static final java.util.Map<String, String> ZH = new java.util.HashMap<>();

    static {
        // 敌对怪
        ZH.put("ZOMBIE", "僵尸"); ZH.put("ZOMBIE_VILLAGER", "僵尸村民"); ZH.put("HUSK", "尸壳"); ZH.put("DROWNED", "溺尸");
        ZH.put("SKELETON", "骷髅"); ZH.put("STRAY", "流浪者"); ZH.put("WITHER_SKELETON", "凋灵骷髅");
        ZH.put("SPIDER", "蜘蛛"); ZH.put("CAVE_SPIDER", "洞穴蜘蛛"); ZH.put("CREEPER", "苦力怕");
        ZH.put("ENDERMAN", "末影人"); ZH.put("WITCH", "女巫"); ZH.put("BLAZE", "烈焰人");
        ZH.put("GHAST", "恶魂"); ZH.put("SLIME", "史莱姆"); ZH.put("MAGMA_CUBE", "岩浆怪");
        ZH.put("WITHER", "凋灵"); ZH.put("ENDER_DRAGON", "末影龙"); ZH.put("PHANTOM", "幻翼");
        ZH.put("SHULKER", "潜影贝"); ZH.put("VINDICATOR", "卫道士"); ZH.put("PILLAGER", "掠夺者");
        ZH.put("EVOKER", "唤魔者"); ZH.put("RAVAGER", "劫掠兽"); ZH.put("WARDEN", "循声守卫");
        ZH.put("BREEZE", "旋风人"); ZH.put("VEX", "恼鬼"); ZH.put("ZOGLIN", "僵尸疣猪兽");
        ZH.put("PIGLIN", "猪灵"); ZH.put("ZOMBIFIED_PIGLIN", "僵尸猪灵"); ZH.put("HOGLIN", "疣猪兽");
        ZH.put("PIGLIN_BRUTE", "猪灵蛮兵"); ZH.put("BOGGED", "沼骸"); ZH.put("BREEZE", "旋风人");
        ZH.put("ENDMITE", "末影螨"); ZH.put("SILVERFISH", "蠹虫"); ZH.put("GUARDIAN", "守卫者");
        ZH.put("ELDER_GUARDIAN", "远古守卫者"); ZH.put("WITHER_SKELETON", "凋灵骷髅");
        // 动物/中立（若 hostile-only=false 也用得上）
        ZH.put("COW", "牛"); ZH.put("PIG", "猪"); ZH.put("SHEEP", "羊"); ZH.put("CHICKEN", "鸡");
        ZH.put("RABBIT", "兔子"); ZH.put("WOLF", "狼"); ZH.put("CAT", "猫"); ZH.put("HORSE", "马");
        ZH.put("DONKEY", "驴"); ZH.put("MULE", "骡"); ZH.put("LLAMA", "羊驼"); ZH.put("MOOSHROOM", "哞菇");
        ZH.put("VILLAGER", "村民"); ZH.put("IRON_GOLEM", "铁傀儡"); ZH.put("SNOWMAN", "雪傀儡");
        ZH.put("BAT", "蝙蝠"); ZH.put("SQUID", "鱿鱼"); ZH.put("GLOW_SQUID", "发光鱿鱼");
        ZH.put("BEE", "蜜蜂"); ZH.put("FOX", "狐狸"); ZH.put("PANDA", "熊猫"); ZH.put("POLAR_BEAR", "北极熊");
        ZH.put("TURTLE", "海龟"); ZH.put("DOLPHIN", "海豚"); ZH.put("AXOLOTL", "美西螈");
        ZH.put("GOAT", "山羊"); ZH.put("ALLAY", "悦灵"); ZH.put("CAMEL", "骆驼");
        // 其他
        ZH.put("PLAYER", "玩家"); ZH.put("ITEM", "物品"); ZH.put("ARMOR_STAND", "盔甲架");
    }

    /** 取怪物中文名（未收录回退英文美化名） */
    public static String name(EntityType type) {
        String zh = ZH.get(type.name());
        if (zh != null) return zh;
        String raw = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    public static String name(String typeName) {
        String zh = ZH.get(typeName);
        if (zh != null) return zh;
        String raw = typeName.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
