package com.elmakers.mine.bukkit.plugins.magic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.spells.AbsorbSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.AlterSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ArrowSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BlastSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BlinkSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BoomSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.BridgeSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ConstructSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.CushionSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.DisintegrateSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FamiliarSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FillSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FireSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FireballSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.FlingSpell;
import com.elmakers.mine.bukkit.plugins.magic.spells.ForceSpell;
import com.elmakers.mine.bukkit.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
import com.nijiko.permissions.PermissionHandler;

public class Spells
{
    /*
     * Public API - Use for hooking up a plugin, or calling a spell
     */

    public Spell getSpell(Material material, Player player)
    {
        Spell spell = spellsByMaterial.get(material);
        if (spell == null || !spell.hasSpellPermission(player))
            return null;

        return getSpell(spell.getName(), player);
    }

    public Spell getSpell(String name, Player player)
    {
        Spell spell = spells.get(name);
        if (spell == null || !spell.hasSpellPermission(player))
            return null;

        PlayerSpells playerSpells = getPlayerSpells(player);
        Spell playerSpell = playerSpells.getSpell(spell.getName());
        if (playerSpell == null)
        {
            playerSpell = (Spell) spell.clone();
            playerSpell.setPlayer(player);
            playerSpells.addSpell(playerSpell);
        }

        return playerSpell;
    }

    public PlayerSpells getPlayerSpells(Player player)
    {
        PlayerSpells spells = playerSpells.get(player.getName());
        if (spells == null)
        {
            spells = new PlayerSpells(player);
            playerSpells.put(player.getName(), spells);
        }
        return spells;
    }

    protected void loadSpells()
    {
        loadSpell(new AbsorbSpell(), "absorb", Material.BUCKET, "Absorb some of the target", "construction", "");
        loadSpell(new AlterSpell(), "alter", Material.REDSTONE_TORCH_ON, "Alter certain objects", "construction", "");
        loadSpell(new ArrowSpell(), "arrow", Material.ARROW, "Fire a magic arrow", "combat", "");
        loadSpell(new ArrowSpell(), "arrowrain", Material.BOW, "Fire a volley of arrows", "combat", "4");
        loadSpell(new BlastSpell(), "blast", Material.SULPHUR, "Mine out a large area", "mining", "");
        loadSpell(new BlastSpell(), "superblast", Material.SLIME_BALL, "Mine out a very large area", "mining", "16");
        loadSpell(new BlinkSpell(), "blink", Material.FEATHER, "Teleport to your target", "psychic", "");
        loadSpell(new BlinkSpell(), "ascend", Material.RED_MUSHROOM, "Go up to the nearest safe spot", "psychic", "ascend");
        loadSpell(new BlinkSpell(), "descend", Material.BROWN_MUSHROOM, "Travel underground", "psychic", "descend");
        loadSpell(new BlinkSpell(), "tesseract", Material.WEB, "Blink a short distance", "psychic", "8");
        loadSpell(new BoomSpell(), "boom", Material.RED_ROSE, "Create an explosion", "combat", "");
        loadSpell(new BoomSpell(), "kaboom", Material.REDSTONE_WIRE, "Create a big explosion", "combat", "6");
        loadSpell(new BoomSpell(), "kamikazee", Material.DEAD_BUSH, "Kill yourself with an explosion", "combat", "8 here");
        loadSpell(new BoomSpell(), "nuke", Material.BED, "Create a huge explosino", "combat", "20");
        loadSpell(new BridgeSpell(), "bridge", Material.GOLD_HOE, "Extend the ground underneath you", "construction", "");
        loadSpell(new ConstructSpell(), "blob", Material.CLAY_BALL, "Create a solid blob", "construction", "sphere 3");
        loadSpell(new ConstructSpell(), "shell", Material.BOWL, "Create a large spherical shell", "construction", "sphere hollow 10");
        loadSpell(new ConstructSpell(), "box", Material.GOLD_HELMET, "Create a large hollow box", "construction", "cuboid hollow 6");
        loadSpell(new ConstructSpell(), "superblob", Material.CLAY_BRICK, "Create a large solid sphere", "construction", "sphere 8");
        loadSpell(new ConstructSpell(), "sandblast", Material.SANDSTONE, "Drop a big block of sand", "combat", "cuboid 4 with sand");
        loadSpell(new CushionSpell(), "cushion", Material.SOUL_SAND, "Create a safety bubble", "help", "");
        loadSpell(new DisintegrateSpell(), "disintegrate", Material.BONE, "Damage your target", "combat", "");
        loadSpell(new FamiliarSpell(), "familiar", Material.EGG, "Create an animal familiar", "farming", "");
        loadSpell(new FamiliarSpell(), "monster", Material.PUMPKIN, "Call a monster to your side", "combat", "monster");
        loadSpell(new FamiliarSpell(), "mob", Material.JACK_O_LANTERN, "Summon a mob of monsters", "combat", "mob 20");
        loadSpell(new FamiliarSpell(), "farm", Material.WHEAT, "Create a herd", "farming", "30");
        loadSpell(new FillSpell(), "fill", Material.GOLD_SPADE, "Fill a selected area (cast twice)", "construction", "");
        loadSpell(new FillSpell(), "paint", Material.PAINTING, "Fill a single block", "alchemy", "single");
        loadSpell(new FillSpell(), "recurse", Material.WOOD_SPADE, "Recursively fill blocks", "alchemy", "recurse");
        loadSpell(new FireballSpell(), "fireball", Material.NETHERRACK, "Cast an exploding fireball", "combat", "", 1500);
        loadSpell(new FireSpell(), "fire", Material.FLINT_AND_STEEL, "Light fires from a distance", "alchemy", "");
        loadSpell(new FireSpell(), "inferno", Material.FIRE, "Burn a wide area", "combat", "6");
        loadSpell(new FlingSpell(), "fling", Material.IRON_BOOTS, "Sends you flying in the target direction", "psychic", "5");
        loadSpell(new FlingSpell(), "leap", Material.LEATHER_BOOTS, "Take a big leap", "psychic", "2");
        loadSpell(new ForceSpell(), "pull", Material.FISHING_ROD, "Pull things toward you", "psychic", "");
        loadSpell(new ForceSpell(), "push", Material.RAILS, "Push things away from you", "psychic", "push");
    }

    public void loadSpell(Spell template, String name, Material icon, String description, String category, String parameterString)
    {
        loadSpell(template, name, icon, description, category, parameterString, 0);
    }

    public void loadSpell(Spell template, String name, Material icon, String description, String category, String parameterString, int cooldown)
    {
        String[] parameters = parameterString.split(" ");
        template.load(name, description, category, icon, parameters, cooldown);
        addSpell(template);
    }

    public void addSpell(Spell variant)
    {
        Spell conflict = spells.get(variant.getName());
        if (conflict != null)
        {
            log.log(Level.WARNING, "Duplicate spell name: '" + conflict.getName() + "'");
        }
        else
        {
            spells.put(variant.getName(), variant);
        }
        Material m = variant.getMaterial();
        if (m != null && m != Material.AIR)
        {
            if (buildingMaterials.contains(m))
            {
                log.warning("Spell " + variant.getName() + " uses building material as icon: " + m.name().toLowerCase());
            }
            conflict = spellsByMaterial.get(m);
            if (conflict != null)
            {
                log.log(Level.WARNING, "Duplicate spell material: " + m.name() + " for " + conflict.getName() + " and " + variant.getName());
            }
            else
            {
                spellsByMaterial.put(variant.getMaterial(), variant);
            }
        }

        variant.initialize(this);
    }

    /*
     * Material use system
     */

    public List<Material> getBuildingMaterials()
    {
        return buildingMaterials;
    }

    /*
     * Undo system
     */

    public UndoQueue getUndoQueue(String playerName)
    {
        UndoQueue queue = playerUndoQueues.get(playerName);
        if (queue == null)
        {
            queue = new UndoQueue();
            queue.setMaxSize(undoQueueDepth);
            playerUndoQueues.put(playerName, queue);
        }
        return queue;
    }

    public void addToUndoQueue(Player player, BlockList blocks)
    {
        UndoQueue queue = getUndoQueue(player.getName());

        queue.add(blocks);
    }

    public boolean undoAny(Player player, Block target)
    {
        for (String playerName : playerUndoQueues.keySet())
        {
            UndoQueue queue = playerUndoQueues.get(playerName);
            if (queue.undo(target))
            {
                if (!player.getName().equals(playerName))
                {
                    player.sendMessage("Undid one of " + playerName + "'s spells");
                }
                return true;
            }
        }

        return false;
    }

    public boolean undo(String playerName)
    {
        UndoQueue queue = getUndoQueue(playerName);
        return queue.undo();
    }

    public boolean undo(String playerName, Block target)
    {
        UndoQueue queue = getUndoQueue(playerName);
        return queue.undo(target);
    }

    public BlockList getLastBlockList(String playerName, Block target)
    {
        UndoQueue queue = getUndoQueue(playerName);
        return queue.getLast(target);
    }

    public BlockList getLastBlockList(String playerName)
    {
        UndoQueue queue = getUndoQueue(playerName);
        return queue.getLast();
    }

    public void scheduleCleanup(BlockList blocks)
    {
        Server server = plugin.getServer();
        BukkitScheduler scheduler = server.getScheduler();

        // scheduler works in ticks- 20 ticks per second.
        long ticksToLive = blocks.getTimeToLive() * 20 / 1000;
        scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(blocks), ticksToLive);
    }

    /*
     * Event registration- call to listen for events
     */

    public void registerEvent(SpellEventType type, Spell spell)
    {
        switch (type)
        {
            case PLAYER_MOVE:
                if (!movementListeners.contains(spell))
                    movementListeners.add(spell);
                break;
            case PLAYER_QUIT:
                if (!quitListeners.contains(spell))
                    quitListeners.add(spell);
                break;
            case PLAYER_DAMAGE:
                if (!damageListeners.contains(spell))
                    damageListeners.add(spell);
                break;
            case PLAYER_DEATH:
                if (!deathListeners.contains(spell))
                    deathListeners.add(spell);
                break;
        }
    }

    public void unregisterEvent(SpellEventType type, Spell spell)
    {
        switch (type)
        {
            case PLAYER_MOVE:
                movementListeners.remove(spell);
                break;
            case PLAYER_DAMAGE:
                damageListeners.remove(spell);
                break;
            case PLAYER_QUIT:
                quitListeners.remove(spell);
                break;
            case PLAYER_DEATH:
                deathListeners.remove(spell);
                break;
        }
    }

    /*
     * Random utility functions
     */

    public int getWandTypeId()
    {
        return wandTypeId;
    }

    public void cancel(Player player)
    {
        PlayerSpells playerSpells = getPlayerSpells(player);
        playerSpells.cancel();
    }

    public boolean isQuiet()
    {
        return quiet;
    }

    public boolean isSilent()
    {
        return silent;
    }

    public boolean isSolid(Material mat)
    {
        return (mat != Material.AIR && mat != Material.WATER && mat != Material.STATIONARY_WATER && mat != Material.LAVA && mat != Material.STATIONARY_LAVA);
    }

    public boolean isSticky(Material mat)
    {
        return stickyMaterials.contains(mat);
    }

    public boolean isStickyAndTall(Material mat)
    {
        return stickyMaterialsDoubleHeight.contains(mat);
    }

    public boolean isAffectedByGravity(Material mat)
    {
        // DOORS are on this list, it's a bit of a hack, but if you consider
        // them
        // as two separate blocks, the top one of which "breaks" when the bottom
        // one does,
        // it applies- but only really in the context of the auto-undo system,
        // so this should probably be its own mat list, ultimately.
        return (mat == Material.GRAVEL || mat == Material.SAND || mat == Material.WOOD_DOOR || mat == Material.IRON_DOOR);
    }

    /*
     * Get the log, if you need to debug or log errors.
     */
    public Logger getLog()
    {
        return log;
    }

    public MagicPlugin getPlugin()
    {
        return plugin;
    }

    /*
     * Internal functions - don't call these, or really anything below here.
     */

    /*
     * Saving and loading
     */

    public void initialize(MagicPlugin plugin)
    {
        this.plugin = plugin;
        load();

        log.info("Magic: Loaded " + spells.size() + " spells.");
    }

    public void load()
    {
        loadProperties();
        loadSpells();
    }

    protected void loadProperties()
    {
        File dataFolder = plugin.getDataFolder();
        dataFolder.mkdirs();
        File pFile = new File(dataFolder, propertiesFile);
        PluginProperties properties = new PluginProperties(pFile.getAbsolutePath());
        properties.load();

        undoQueueDepth = properties.getInteger("spells-general-undo-depth", undoQueueDepth);
        silent = properties.getBoolean("spells-general-silent", silent);
        quiet = properties.getBoolean("spells-general-quiet", quiet);
        stickyMaterials = PluginProperties.parseMaterials(STICKY_MATERIALS);
        stickyMaterialsDoubleHeight = PluginProperties.parseMaterials(STICKY_MATERIALS_DOUBLE_HEIGHT);

        buildingMaterials = properties.getMaterials("spells-general-building", DEFAULT_BUILDING_MATERIALS);
        wandTypeId = properties.getInteger("wand-type-id", wandTypeId);

        for (Spell spell : spells.values())
        {
            spell.onLoad(properties);
        }

        properties.save();
    }

    public void clear()
    {
        movementListeners.clear();
        damageListeners.clear();
        quitListeners.clear();
        spells.clear();
        spellsByMaterial.clear();
    }

    /*
     * Listeners / callbacks
     */

    public void onPlayerQuit(PlayerQuitEvent event)
    {
        // Must allow listeners to remove themselves during the event!
        List<Spell> active = new ArrayList<Spell>();
        active.addAll(quitListeners);
        for (Spell listener : active)
        {
            listener.onPlayerQuit(event);
        }
    }

    public void onPlayerMove(PlayerMoveEvent event)
    {
        // Must allow listeners to remove themselves during the event!
        List<Spell> active = new ArrayList<Spell>();
        active.addAll(movementListeners);
        for (Spell listener : active)
        {
            listener.onPlayerMove(event);
        }
    }

    public void onPlayerDeath(Player player, EntityDeathEvent event)
    {
        // Must allow listeners to remove themselves during the event!
        /*
         * Disabled for now- multi-world issues List<Spell> active = new
         * ArrayList<Spell>(); active.addAll(deathListeners); for (Spell
         * listener : active) { listener.onPlayerDeath(player, event); }
         */
    }

    public void onPlayerDamage(Player player, EntityDamageEvent event)
    {
        List<Spell> active = new ArrayList<Spell>();
        active.addAll(damageListeners);
        for (Spell listener : active)
        {
            listener.onPlayerDamage(player, event);
        }
    }

    public List<Spell> getAllSpells()
    {
        List<Spell> allSpells = new ArrayList<Spell>();
        allSpells.addAll(spells.values());
        return allSpells;
    }

    /**
     * Called when a player plays an animation, such as an arm swing
     * 
     * @param event
     *            Relevant event details
     */
    public void onPlayerAnimation(PlayerAnimationEvent event)
    {
        Player player = event.getPlayer();
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
        {
            if (event.getPlayer().getInventory().getItemInHand().getTypeId() == getWandTypeId())
            {
                if (!hasWandPermission(player))
                {
                    return;
                }

                Inventory inventory = player.getInventory();
                ItemStack[] contents = inventory.getContents();

                Spell spell = null;
                for (int i = 0; i < 9; i++)
                {
                    if (contents[i].getType() == Material.AIR || contents[i].getTypeId() == getWandTypeId())
                    {
                        continue;
                    }
                    spell = getSpell(contents[i].getType(), player);
                    if (spell != null)
                    {
                        break;
                    }
                }

                if (spell != null)
                {
                    spell.cast();
                }

            }
        }
    }

    @SuppressWarnings("deprecation")
    public boolean cycleMaterials(Player player)
    {
        List<Material> buildingMaterials = getBuildingMaterials();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        int firstMaterialSlot = 8;
        boolean foundAir = false;

        for (int i = 8; i >= 0; i--)
        {
            Material mat = contents[i] == null ? Material.AIR : contents[i].getType();
            if (mat == Material.AIR)
            {
                if (foundAir)
                {
                    break;
                }
                else
                {
                    foundAir = true;
                    firstMaterialSlot = i;
                    continue;
                }
            }
            else
            {
                if (buildingMaterials.contains(mat))
                {
                    firstMaterialSlot = i;
                    continue;
                }
                else
                {
                    break;
                }
            }
        }

        if (firstMaterialSlot == 8)
            return false;

        ItemStack lastSlot = contents[8];
        for (int i = 7; i >= firstMaterialSlot; i--)
        {
            contents[i + 1] = contents[i];
        }
        contents[firstMaterialSlot] = lastSlot;

        inventory.setContents(contents);
        player.updateInventory();

        return true;
    }

    @SuppressWarnings("deprecation")
    public void cycleSpells(Player player)
    {
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] active = new ItemStack[9];

        for (int i = 0; i < 9; i++)
        {
            active[i] = contents[i];
        }

        int maxSpellSlot = 0;
        int firstSpellSlot = -1;
        for (int i = 0; i < 9; i++)
        {
            boolean isEmpty = active[i] == null;
            Material activeType = isEmpty ? Material.AIR : active[i].getType();
            boolean isWand = activeType.getId() == getWandTypeId();
            boolean isSpell = false;
            if (activeType != Material.AIR)
            {
                Spell spell = getSpell(activeType, player);
                isSpell = spell != null;
            }

            if (isSpell)
            {
                if (firstSpellSlot < 0)
                    firstSpellSlot = i;
                maxSpellSlot = i;
            }
            else
            {
                if (!isWand && firstSpellSlot >= 0)
                {
                    break;
                }
            }

        }

        int numSpellSlots = firstSpellSlot < 0 ? 0 : maxSpellSlot - firstSpellSlot + 1;

        if (numSpellSlots < 2)
        {
            return;
        }

        for (int ddi = 0; ddi < numSpellSlots; ddi++)
        {
            int i = ddi + firstSpellSlot;
            Material contentsType = contents[i] == null ? Material.AIR : active[i].getType();
            if (contentsType.getId() != getWandTypeId())
            {
                for (int di = 1; di < numSpellSlots; di++)
                {
                    int dni = (ddi + di) % numSpellSlots;
                    int ni = dni + firstSpellSlot;
                    Material activeType = active[ni] == null ? Material.AIR : active[ni].getType();
                    if (activeType.getId() != getWandTypeId())
                    {
                        contents[i] = active[ni];
                        break;
                    }
                }
            }
        }

        inventory.setContents(contents);
        player.updateInventory();
    }

    /**
     * Called when a player uses an item
     * 
     * @param event
     *            Relevant event details
     */
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            cancel(event.getPlayer());

            int materialId = event.getPlayer().getInventory().getItemInHand().getTypeId();
            Player player = event.getPlayer();

            if (!hasWandPermission(player))
            {
                return;
            }

            boolean cycleSpells = false;

            cycleSpells = player.isSneaking();
            if (materialId == getWandTypeId())
            {
                if (cycleSpells)
                {
                    if (!cycleMaterials(event.getPlayer()))
                    {
                        cycleSpells(event.getPlayer());
                    }
                }
                else
                {
                    cycleSpells(event.getPlayer());
                }
            }
        }
    }

    public boolean allowPhysics(Block block)
    {
        if (physicsDisableTimeout == 0)
            return true;
        if (System.currentTimeMillis() > physicsDisableTimeout)
            physicsDisableTimeout = 0;
        return false;
    }

    public void disablePhysics(int interval)
    {
        physicsDisableTimeout = System.currentTimeMillis() + interval;
    }

    public boolean hasWandPermission(Player player)
    {
        return hasPermission(player, "Magic.wand.use");
    }

    public boolean hasPermission(Player player, String pNode, boolean defaultValue)
    {
        PermissionHandler permissions = MagicPlugin.getPermissionHandler();
        if (permissions == null)
        {
            return defaultValue;
        }

        return permissions.has(player, pNode);
    }

    public boolean hasPermission(Player player, String pNode)
    {
        return hasPermission(player, pNode, true);
    }

    /*
     * Private data
     */
    private final String                        propertiesFile                 = "magic.properties";
    private int                                 wandTypeId                     = 280;

    static final String                         DEFAULT_BUILDING_MATERIALS     = "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,33,34,35,41,42,43,45,46,47,48,49,52,53,55,56,57,58,60,61,62,65,66,67,73,74,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96";
    static final String                         STICKY_MATERIALS               = "37,38,39,50,51,55,59,63,64,65,66,68,70,71,72,75,76,77,78,83";
    static final String                         STICKY_MATERIALS_DOUBLE_HEIGHT = "64,71,";

    private List<Material>                      buildingMaterials              = new ArrayList<Material>();
    private List<Material>                      stickyMaterials                = new ArrayList<Material>();
    private List<Material>                      stickyMaterialsDoubleHeight    = new ArrayList<Material>();

    private long                                physicsDisableTimeout          = 0;
    private int                                 undoQueueDepth                 = 256;
    private boolean                             silent                         = false;
    private boolean                             quiet                          = true;
    private HashMap<String, UndoQueue>          playerUndoQueues               = new HashMap<String, UndoQueue>();

    private final Logger                        log                            = Logger.getLogger("Minecraft");
    private final HashMap<String, Spell>        spells                         = new HashMap<String, Spell>();
    private final HashMap<Material, Spell>      spellsByMaterial               = new HashMap<Material, Spell>();
    private final HashMap<String, PlayerSpells> playerSpells                   = new HashMap<String, PlayerSpells>();
    private final List<Spell>                   movementListeners              = new ArrayList<Spell>();
    private final List<Spell>                   quitListeners                  = new ArrayList<Spell>();
    private final List<Spell>                   deathListeners                 = new ArrayList<Spell>();
    private final List<Spell>                   damageListeners                = new ArrayList<Spell>();

    private MagicPlugin                         plugin                         = null;
}
