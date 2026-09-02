package nl.tinyaii.tinymoblevels.util;

import nl.tinyaii.tinymoblevels.TinyMobLevelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 经济桥（软依赖/反射）：自家 Economy 优先 → Vault 次之 → 都没装则金币不可用。
 * 与全家桶 EcoBridge 同构。
 */
public class EcoBridge {

    private final TinyMobLevelsPlugin plugin;
    private Method mDeposit;
    private Object vaultEconomy;
    private boolean own = false;
    private boolean vault = false;

    public EcoBridge(TinyMobLevelsPlugin plugin) {
        this.plugin = plugin;
        tryInit();
    }

    public void tryInit() {
        own = false;
        vault = false;
        if (Bukkit.getPluginManager().getPlugin("Economy") != null) {
            try {
                Class<?> api = Class.forName("nl.tinyaii.economy.api.EconomyAPI");
                mDeposit = api.getMethod("deposit", UUID.class, double.class);
                own = true;
                plugin.getLogger().info("[经济桥] 已对接自家 Economy");
                return;
            } catch (Throwable ignored) {}
        }
        try {
            Class<?> ecoClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(ecoClass);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
                vault = true;
                plugin.getLogger().info("[经济桥] 已对接 Vault");
            }
        } catch (Throwable ignored) {}
    }

    public boolean isAvailable() { return own || vault; }

    /** 给玩家加金币；返回是否成功 */
    public boolean deposit(Player p, double amount) {
        if (amount <= 0) return false;
        if (own) {
            try { mDeposit.invoke(null, p.getUniqueId(), amount); return true; }
            catch (Exception e) { return false; }
        }
        if (vault) {
            try {
                vaultEconomy.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class)
                        .invoke(vaultEconomy, p, amount);
                return true;
            } catch (Exception e) { return false; }
        }
        return false;
    }
}
