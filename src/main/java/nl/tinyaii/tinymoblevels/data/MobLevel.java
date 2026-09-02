package nl.tinyaii.tinymoblevels.data;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;

/**
 * 怪物等级工具：用 PDC 标记等级 + 实际防御（血量溢出折算后的最终防御）。
 */
public class MobLevel {

    private final NamespacedKey levelKey;
    private final NamespacedKey defenseKey;

    public MobLevel(NamespacedKey levelKey) {
        this.levelKey = levelKey;
        this.defenseKey = new NamespacedKey(levelKey.getNamespace(), "mob_defense");
    }

    /** 标记怪物等级 + 实际防御（用于减伤） */
    public void setLevel(LivingEntity entity, int level, double defense) {
        entity.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        entity.getPersistentDataContainer().set(defenseKey, PersistentDataType.DOUBLE, defense);
    }

    /** 读取怪物等级（未标记返回 0） */
    public int getLevel(LivingEntity entity) {
        Integer level = entity.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    /** 读取实际防御（未标记返回 1.0 = 无减伤） */
    public double getDefense(LivingEntity entity) {
        Double defense = entity.getPersistentDataContainer().get(defenseKey, PersistentDataType.DOUBLE);
        return defense == null ? 1.0 : defense;
    }

    /** 是否分级怪 */
    public boolean isLeveled(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER);
    }
}
