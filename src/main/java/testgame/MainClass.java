package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.Listener;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBookEnchanted;
import cn.nukkit.item.ItemEmerald;
import cn.nukkit.item.ItemTotem;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import gameapi.arena.Arena;
import gameapi.effect.Effect;
import gameapi.room.Room;
import gameapi.room.RoomRule;
import gameapi.room.RoomStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainClass extends PluginBase implements Listener {

    public static List<Room> roomListHashMap = new ArrayList<>();
    public static HashMap<String, List<Effect>> effectHashMap = new HashMap<>();
    public static String path = null;
    public static Map<String, Object> scoreboardCfg = new HashMap<String, Object>();

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.getLogger().info("欢迎使用本插件【RecknessHero】");
        this.getLogger().info("您安装的本插件不需要收费购买，作者minebbs昵称:Glorydark！");
        this.getServer().getPluginManager().registerEvents(this,this);
        this.getServer().getPluginManager().registerEvents(new Event(),this);
        this.getServer().getCommandMap().register("暴走英雄",new GameCommand("drh"));
        path = getDataFolder().getPath();
        this.saveResource("blockaddons.yml",false);
        this.saveResource("rooms.yml",false);
        this.saveResource("scoreboard.yml",false);
        this.loadRooms();
        this.loadBlockAddons();
        this.loadScoreboardSetting();
        super.onEnable();
    }

    public void loadScoreboardSetting(){
        this.getLogger().info("正在加载计分板设置...");
        Config config = new Config(this.getDataFolder()+"/scoreboard.yml",Config.YAML);
        scoreboardCfg = config.getAll();
        this.getLogger().info("记分板设置加载成功！");
    }

    public static String getScoreboardSetting(String key){
        if(scoreboardCfg.containsKey(key)){
            return String.valueOf(scoreboardCfg.get(key));
        }else{
            return "null";
        }
    }

    public void loadBlockAddons(){
        Config config = new Config(this.getDataFolder()+"/blockaddons.yml",Config.YAML);
        effectHashMap = new HashMap<>();
        for(String string: config.getKeys(false)){
            this.getLogger().info("正在加载方块"+string+"的拓展数据");
            String[] idSplit = string.split(":");
            for(Room room:roomListHashMap){
                RoomRule roomRule = room.getRoomRule();
                roomRule.canBreakBlocks.add(Integer.valueOf(idSplit[0]));
                room.setRoomRule(roomRule);
                this.getLogger().info("方块"+idSplit[0]+"已被允许在游戏中破坏");
            }
            List<Effect> effectList = new ArrayList<>();
            for(String effectStr: config.getStringList(string+".effects")) {
                String[] effectSplit = effectStr.split(":");
                if(effectSplit.length == 3) {
                   gameapi.effect.Effect effect = new gameapi.effect.Effect(Integer.parseInt(effectSplit[0]),Integer.parseInt(effectSplit[1]),Integer.parseInt(effectSplit[2]));
                    effectList.add(effect);
                }
            }
            effectHashMap.put(string,effectList);
        }
        this.getLogger().info("方块拓展加载成功！");
    }

    public void loadRooms(){
        Config config = new Config(this.getDataFolder()+"/rooms.yml",Config.YAML);
        if(config.getKeys() != null){
            for(String s:config.getKeys(false)){
                RoomRule roomRule = new RoomRule(0);
                roomRule.allowBreakBlock = false;
                roomRule.allowPlaceBlock = false;
                roomRule.allowFallDamage = false;
                roomRule.allowDamagePlayer = false;
                roomRule.allowHungerDamage = false;
                roomRule.allowFoodLevelChange = false;
                roomRule.canBreakBlocks.add(100);
                roomRule.canBreakBlocks.add(152);
                roomRule.canPlaceBlocks.add(152);
                Room room = new Room("DRecknessHero", roomRule, "",1);
                if (config.exists(s + ".LoadWorld")) {
                    String backup = config.getString(s + ".LoadWorld");
                    room.setRoomLevelBackup(backup);
                    room.setStartLevel(s);
                    if (Server.getInstance().getLevelByName(config.getString(s)) == null) {
                        if(Arena.copyWorldAndLoad(s, backup)){
                            if (Server.getInstance().isLevelLoaded(s)) {
                                Server.getInstance().getLevelByName(s).setAutoSave(false);
                            } else {
                                this.getLogger().info("房间【" + s + "】检测已经加载！");
                            }
                        }
                    } else {
                        this.getLogger().info("房间【" + s + "】加载失败,请检查地图是否存在！");
                        continue;
                    }
                }else{
                    this.getLogger().info("房间【" + s + "】加载失败,请检查地图是否存在！");
                    continue;
                }

                if(config.exists(s+".WaitSpawn")){
                    String[] strings = config.getString(s+".WaitSpawn").split(":");
                    if(strings.length != 3){ this.getLogger().info("房间【"+s+"】加载失败,请检查出生地配置！");return;}
                    room.setWaitSpawn(new Position(Double.parseDouble(strings[0]),Double.parseDouble(strings[1]),Double.parseDouble(strings[2]), Server.getInstance().getLevelByName(s)).getLocation());
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查等待点配置！");
                    continue;
                }

                if(config.exists(s+".StartSpawn")){
                    String[] strings = config.getString(s+".StartSpawn").split(":");
                    if(strings.length != 3){ this.getLogger().info("房间【"+s+"】加载失败,请检查出生地配置！");return;}
                    room.setStartSpawn(new Position(Double.parseDouble(strings[0]),Double.parseDouble(strings[1]),Double.parseDouble(strings[2]), Server.getInstance().getLevelByName(s)).getLocation());
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查出生地配置！");
                    continue;
                }

                if(config.exists(s+".WaitTime")){
                    room.setWaitTime(config.getInt(s+".WaitTime"));
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查等待时间配置！");
                    continue;
                }

                if(config.exists(s+".GameTime")){
                    room.setGameTime(config.getInt(s+".GameTime"));
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查游戏时间配置！");
                    continue;
                }

                if(config.exists(s+".MinPlayer")){
                    room.setMinPlayer(config.getInt(s+".MinPlayer",1));
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查最小玩家人数配置！");
                    continue;
                }

                if(config.exists(s+".MaxPlayer")){
                    room.setMinPlayer(config.getInt(s+".MaxPlayer",1));
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查最大玩家人数配置！");
                    continue;
                }
                Event.roomFinishPlayers.put(room,new ArrayList<>());
                Room.loadRoom(room);
                roomListHashMap.add(room);
                room.setRoomStatus(RoomStatus.ROOM_STATUS_WAIT);
                room.setRoomName(s);
                this.getLogger().info("房间【"+s+"】加载成功！");
            }
        }
    }

    public static void processJoin(Room room, Player p){
        if(room != null){
            if(Server.getInstance().isLevelLoaded(room.getStartLevel())) {
                RoomStatus rs = room.getRoomStatus();
                if(rs == RoomStatus.ROOM_STATUS_WAIT || rs == RoomStatus.ROOM_STATUS_PreStart) {
                    if(room.addPlayer(p)) {
                        p.getInventory().clearAll();
                        p.getUIInventory().clearAll();
                        p.getFoodData().setLevel(p.getFoodData().getMaxLevel());
                        p.teleport(Position.fromObject(room.getWaitSpawn().getLocation(), Server.getInstance().getLevelByName(room.getStartLevel())));
                        p.setGamemode(2);
                        Item addItem1 = new ItemBookEnchanted();
                        addItem1.setCustomName("§l§c退出房间");
                        p.getInventory().setItem(0,addItem1);

                        Item addItem2 = new ItemEmerald();
                        addItem2.setCustomName("§l§a历史战绩");
                        p.getInventory().setItem(7,addItem2);

                        Item addItem3 = new ItemTotem(0);
                        addItem3.setCustomName("§l§e选择职业");
                        p.getInventory().setItem(8,addItem3);

                        room.setPlayerProperties(p, "skill1", 0);
                    }else{
                        p.sendMessage("房间人数已满！");
                    }
                }else{
                    p.sendMessage("游戏已经开始！");
                }
            }else{
                p.sendMessage("地图未加载完毕！");
            }
        }else{
            p.sendMessage("该房间不存在！");
        }
    }

    public static List<Effect> getBlockAddonsInit(Block block){
        int blockid = block.getId();
        int blockmeta = block.getDamage();
        String s = blockid+":"+blockmeta;
        for(String string: MainClass.effectHashMap.keySet()){
            if(s.equals(string)){
                return MainClass.effectHashMap.get(string);
            }
        }
        return null;
    }
}
