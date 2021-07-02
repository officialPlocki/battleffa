package de.battlesucht.battleffa.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.battlesucht.api.utils.files.FileBuilder;
import de.battlesucht.api.utils.player.Language;
import de.battlesucht.api.utils.server.global.BitsAPI;
import de.battlesucht.battleffa.BattleFFA;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryHandler implements Listener {
    private static HashMap<Player, Player> attacker = new HashMap<>();

    private static Location loc = getLocation();

    private static FileBuilder fb = new FileBuilder("locations.yml");
    private static YamlConfiguration yml = fb.getYaml();

    public static Location getLocation() {
        if(yml.isSet("Spawn.world") == false) {
            yml.set("Spawn.World", "world");
            yml.set("Spawn.X", 0.0);
            yml.set("Spawn.Y", 0.0);
            yml.set("Spawn.Z", 0.0);
            yml.set("Spawn.Yaw", (float) 0);
            yml.set("Spawn.Pitch", (float) 0);
            fb.save();
        }
        return new Location(Bukkit.getWorld(yml.getString("Spawn.World")), yml.getDouble("Spawn.X"), yml.getDouble("Spawn.Y"), yml.getDouble("Spawn.Z"), (float) yml.get("Spawn.Yaw"), (float) yml.get("Spawn.Pitch"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        BattleFFA.setSidebar(e.getPlayer());
        e.getPlayer().teleport(loc);
        setInventory(e.getPlayer(), "up");
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    private static List<String> knock = new ArrayList<>();

    private static List<String> blocks = new ArrayList<>();

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (!e.getEntityType().equals(EntityType.PLAYER))
            e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    private static void addPerk(Player p, Perks perk) {
        yml.set(p.getUniqueId()+".perk."+perk.name(), true);
        fb.save();
    }

    private static boolean hasPerk(Player p, Perks perk) {
        if(!yml.isSet(p.getUniqueId()+".perk."+perk.name())) {
            yml.set(p.getUniqueId()+".perk."+perk.name(), false);
            fb.save();
        }
        return yml.getBoolean(p.getUniqueId()+".perk."+perk.name());
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent e) {
        if (e.getBlock().getLocation().getBlockY() >= 43) {
            e.setCancelled(true);
        } else {
            e.setCancelled(false);
            if (hasPerk(e.getPlayer(), Perks.BLOCKS))
                e.getPlayer().getItemInHand().setAmount(64);
            Bukkit.getScheduler().runTaskLater(BattleFFA.plugin, () -> e.getBlock().setType(Material.REDSTONE_BLOCK),  60L);
            Bukkit.getScheduler().runTaskLater(BattleFFA.plugin, () -> e.getBlock().setType(Material.AIR),  100L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null) {
            if (e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§8» §bShop §8«")) {
                knock.add("§8");
                knock.add("§8» §e8000 Bits");
                knock.add("§8");
                blocks.add("§8");
                blocks.add("§8» §e4000 Bits");
                blocks.add("§8");
                Inventory inv = Bukkit.createInventory(null, 27, "§bShop");
                ItemStack panein = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
                ItemMeta paneinMeta = panein.getItemMeta();
                paneinMeta.setDisplayName("§k");
                panein.setItemMeta(paneinMeta);
                for (int i = 0; i < inv.getSize(); i++)
                    inv.setItem(i, panein);
                ItemStack knockback3 = new ItemStack(Material.STICK);
                ItemMeta knockback3Mata = knockback3.getItemMeta();
                knockback3Mata.setDisplayName("§8» §aPermanentes KnockBack 3 §8«");
                knockback3Mata.addEnchant(Enchantment.KNOCKBACK, 3, true);
                knockback3Mata.setLore(knock);
                knockback3.setItemMeta(knockback3Mata);
                ItemStack infinity = new ItemStack(Material.SANDSTONE, -1);
                ItemMeta infinityMeta = infinity.getItemMeta();
                infinityMeta.setDisplayName("§8» §eUnendlich Blöcke §8«");
                infinityMeta.setLore(blocks);
                infinity.setItemMeta(infinityMeta);
                blocks.clear();
                knock.clear();
                inv.setItem(11, knockback3);
                inv.setItem(15, infinity);
                e.getPlayer().openInventory(inv);
            }
        } else {
            return;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() != null) {
            if (e.getView().getTitle().equalsIgnoreCase("§bShop") &&
                    e.getCurrentItem() != null)
                if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§8» §aPermanentes KnockBack 3 §8«")) {
                    if (hasPerk((Player) e.getWhoClicked(), Perks.KNOCKBACK)) {
                        e.getWhoClicked().sendMessage(Language.prefix +"Du hast bereits dieses Upgrade gekauft.");
                    } else if (BitsAPI.getBits((Player) e.getWhoClicked()) == 8000 || BitsAPI.getBits((Player) e.getWhoClicked()) >= 8000) {
                        BitsAPI.removeBits((Player) e.getWhoClicked(), 8000);
                        addPerk((Player) e.getWhoClicked(), Perks.KNOCKBACK);
                        e.getWhoClicked().sendMessage(Language.prefix +"Du hast dir erfolgreich das Permanente KnockBack 3 Upgrade gekauft.");
                        e.getWhoClicked().closeInventory();
                    } else {
                        e.getWhoClicked().sendMessage(Language.prefix +"Du hast nicht genügend Bits.");
                    }
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§8» §eUnendlich Blöcke §8«")) {
                    if (hasPerk((Player) e.getWhoClicked(), Perks.BLOCKS)) {
                        e.getWhoClicked().sendMessage(Language.prefix +"Du hast bereits dieses Upgrade gekauft.");
                    } else if (BitsAPI.getBits((Player) e.getWhoClicked()) == 4000 || BitsAPI.getBits((Player) e.getWhoClicked()) >= 4000) {
                        BitsAPI.removeBits((Player) e.getWhoClicked(), 4000);
                        addPerk((Player) e.getWhoClicked(), Perks.BLOCKS);
                        e.getWhoClicked().sendMessage(Language.prefix +"Du hast dir erfolgreich das Unendliche Blöcke Upgrade gekauft.");
                        e.getWhoClicked().closeInventory();
                    } else {
                        e.getWhoClicked().sendMessage(Language.prefix +"Du hast nicht genügend Bits.");
                    }
                }
            if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§8» §bShop §8«")) {
                knock.add("§8");
                knock.add("§8» §e8000 Bits");
                knock.add("§8");
                blocks.add("§8");
                blocks.add("§8» §e4000 Bits");
                blocks.add("§8");
                Inventory inv = Bukkit.createInventory(null, 27, "§bShop");
                ItemStack panein = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
                ItemMeta paneinMeta = panein.getItemMeta();
                paneinMeta.setDisplayName("§k");
                panein.setItemMeta(paneinMeta);
                for (int i = 0; i < inv.getSize(); i++)
                    inv.setItem(i, panein);
                ItemStack knockback3 = new ItemStack(Material.STICK);
                ItemMeta knockback3Mata = knockback3.getItemMeta();
                knockback3Mata.setDisplayName("§8» §aPermanentes KnockBack 3 §8«");
                knockback3Mata.addEnchant(Enchantment.KNOCKBACK, 3, true);
                knockback3Mata.setLore(knock);
                knockback3.setItemMeta(knockback3Mata);
                ItemStack infinity = new ItemStack(Material.SANDSTONE, -1);
                ItemMeta infinityMeta = infinity.getItemMeta();
                infinityMeta.setDisplayName("§8» §eUnendlich Blöcke §8«");
                infinityMeta.setLore(blocks);
                infinity.setItemMeta(infinityMeta);
                blocks.clear();
                knock.clear();
                inv.setItem(11, knockback3);
                inv.setItem(15, infinity);
                e.getWhoClicked().openInventory(inv);
            } else {
                return;
            }
        } else {
            return;
        }
    }

    public static void setInventory(Player p, String inv) {
        if (inv.equalsIgnoreCase("down")) {
            p.getInventory().clear();
            ItemStack stick = new ItemStack(Material.STICK);
            ItemStack blocks = new ItemStack(Material.SANDSTONE, 64);
            ItemMeta stickMeta = stick.getItemMeta();
            stickMeta.setDisplayName("§8» §eKnockBack Stick §8«");
            if (hasPerk(p, Perks.KNOCKBACK)) {
                stickMeta.addEnchant(Enchantment.KNOCKBACK, 3, true);
            } else {
                stickMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            }
            stick.setItemMeta(stickMeta);
            p.getInventory().clear();
            p.getInventory().setItem(0, stick);
            p.getInventory().setItem(8, blocks);
            p.getInventory().setItem(7, blocks);
        } else if (inv.equalsIgnoreCase("up")) {
            ItemStack shop = new ItemStack(Material.ANVIL);
            ItemMeta shopMeta = shop.getItemMeta();
            shopMeta.setDisplayName("§8» §bShop §8«");
            shop.setItemMeta(shopMeta);
            p.getInventory().clear();
            p.getInventory().setItem(4, shop);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo().getY() <= 43.0D) {
            if (e.getPlayer().getInventory().contains(Material.CHEST))
                setInventory(e.getPlayer(), "down");
        } else if (e.getTo().getY() >= 43.0D &&
                e.getPlayer().getInventory().contains(Material.STICK)) {
            setInventory(e.getPlayer(), "up");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.setRespawnLocation(loc);
        e.getRespawnLocation().setX(loc.getX());
        e.getRespawnLocation().setY(loc.getY());
        e.getRespawnLocation().setZ(loc.getZ());
        e.getRespawnLocation().setWorld(loc.getWorld());
        e.getRespawnLocation().setYaw(loc.getYaw());
        e.getRespawnLocation().setPitch(loc.getPitch());
        e.getRespawnLocation().setDirection(loc.getDirection());
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        setInventory(e.getPlayer(), "up");
        e.getPlayer().teleport(loc);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        e.setDroppedExp(0);
        BitsAPI.removeBits(e.getEntity(), 10);
        if (BitsAPI.getBits(e.getEntity()) <= 0)
            BitsAPI.setBits(e.getEntity(), 0);
        BitsAPI.addBits(attacker.get(e.getEntity()), 10);
        e.getEntity().setHealth(20.0D);
        e.getEntity().getInventory().clear();
        setInventory(e.getEntity(), "up");
        e.getEntity().teleport(loc);
    }

    @EventHandler
    public void onDamage2(EntityDamageEvent e) {
        if (e.getEntityType().equals(EntityType.PLAYER)) {
            if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                e.setCancelled(true);
                if (e.getEntity().getLocation().getY() <= 43.0D)
                    setInventory((Player)e.getEntity(), "down");
            }
            if (e.getEntity().getLocation().getY() <= 0.0D) {
                e.setCancelled(true);
                if (attacker.containsKey(e.getEntity())) {
                    e.getEntity().sendMessage(Language.prefix +"Du wurdest von §e" + ((Player)attacker.get(e.getEntity())).getName() + "§7 getötet und hast 10 Bits verloren.");
                    ((Player)attacker.get(e.getEntity())).sendMessage(Language.prefix +"Du hast 10 Bits erhalten, weil du §e" + e.getEntity().getName() + "§7 getötet hast.");
                    BitsAPI.removeBits((Player)e.getEntity(), 10);
                    BitsAPI.addBits(attacker.get(e.getEntity()), 10);
                    if (BitsAPI.getBits((Player)e.getEntity()) <= 0)
                        BitsAPI.setBits((Player)e.getEntity(), 0);
                    ((Player)attacker.get(e.getEntity())).setStatistic(Statistic.PLAYER_KILLS, ((Player)attacker.get(e.getEntity())).getStatistic(Statistic.PLAYER_KILLS) + 1);
                    ((Player)e.getEntity()).setStatistic(Statistic.DEATHS, ((Player)e.getEntity()).getStatistic(Statistic.DEATHS) + 1);
                } else {
                    e.getEntity().sendMessage(Language.prefix +"Du bist gestorben und hast 10 Bits verloren.");
                }
                ((Player)e.getEntity()).getInventory().clear();
                setInventory((Player)e.getEntity(), "up");
                e.getEntity().teleport(loc);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType().equals(EntityType.PLAYER) &&
                e.getEntity().getType().equals(EntityType.PLAYER)) {
            if (e.getEntity().getLocation().getY() >= 43.0D) {
                e.setCancelled(true);
                return;
            }
            attacker.put((Player)e.getEntity(), (Player)e.getDamager());
            e.setDamage(0.0D);
        }
    }
}
