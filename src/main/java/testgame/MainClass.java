package testgame;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import gameapi.arena.Arena;
import gameapi.effect.Effect;
import gameapi.room.DefaultRoomRule;
import gameapi.room.Room;
import gameapi.room.RoomStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainClass extends PluginBase implements Listener {

    public static List<Room> roomListHashMap = new ArrayList<>();
    public static HashMap<String, List<gameapi.effect.Effect>> effectHashMap = new HashMap<>();
    public static String path = null;

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
        this.getServer().getCommandMap().register("暴走英雄",new Command("drh"));
        path = getDataFolder().getPath();
        this.saveResource("blockaddons.yml",false);
        this.saveResource("rooms.yml",false);
        this.loadRooms();
        this.loadBlockAddons();
        super.onEnable();
    }

    public static List<gameapi.effect.Effect> getBlockAddonsInit(Block block){
        int blockid = block.getId();
        int blockmeta = block.getDamage();
        String s = blockid+":"+blockmeta;
        for(String string:effectHashMap.keySet()){
            if(s.equals(string)){
                return effectHashMap.get(string);
            }
        }
        return null;
    }

    public void loadBlockAddons(){
        Config config = new Config(this.getDataFolder()+"/blockaddons.yml",Config.YAML);
        effectHashMap = new HashMap<>();
        for(String string: config.getKeys(false)){
            this.getLogger().info("正在加载方块"+string+"的拓展数据");
            String[] idSplit = string.split(":");
            for(Room room:roomListHashMap){
                room.roomRule.canBreakBlocks.add(Integer.valueOf(idSplit[0]));
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
                DefaultRoomRule defaultRoomRule = new DefaultRoomRule(0,true,0);
                defaultRoomRule.canBreakBlocks.add(100);
                defaultRoomRule.canBreakBlocks.add(152);
                defaultRoomRule.canPlaceBlocks.add(152);
                Room room = new Room(defaultRoomRule,1);
                if(config.exists(s+".LoadWorld")){
                    this.getLogger().info("正在准备世界，房间:"+s);
                    if(Arena.copyWorldAndLoad(s + "&worldload",config.getString(s + ".LoadWorld"))) {
                        if (Server.getInstance().getLevelByName(config.getString(s + ".LoadWorld")) == null) {
                            if (Server.getInstance().isLevelLoaded(s + "&worldload")) {
                                Server.getInstance().getLevelByName(s + "&worldload").setAutoSave(false);
                            } else {
                                this.getLogger().info("房间【" + s + "】加载失败,请检查加载地图是否存在！");
                                return;
                            }
                        } else {
                            this.getLogger().info("房间【" + s + "】加载失败,请检查地图是否存在！");
                            return;
                        }
                    }
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查地图备份是否存在！");
                    return;
                }

                if(config.exists(s+".WaitSpawn")){
                    String[] strings = config.getString(s+".WaitSpawn").split(":");
                    if(strings.length != 3){ this.getLogger().info("房间【"+s+"】加载失败,请检查出生地配置！");return;}
                    room.waitSpawn = new Position(Double.parseDouble(strings[0]),Double.parseDouble(strings[1]),Double.parseDouble(strings[2]), Server.getInstance().getLevelByName(s+ "&worldload"));
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查等待点配置！");
                    return;
                }

                if(config.exists(s+".StartSpawn")){
                    String[] strings = config.getString(s+".StartSpawn").split(":");
                    if(strings.length != 3){ this.getLogger().info("房间【"+s+"】加载失败,请检查出生地配置！");return;}
                    room.startSpawn = new Position(Double.parseDouble(strings[0]),Double.parseDouble(strings[1]),Double.parseDouble(strings[2]), Server.getInstance().getLevelByName(s+ "&worldload"));
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查出生地配置！");
                    return;
                }

                if(config.exists(s+".WaitTime")){
                    room.waitTime = config.getInt(s+".WaitTime");
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查等待时间配置！");
                    return;
                }

                if(config.exists(s+".GameTime")){
                    room.gameTime = config.getInt(s+".GameTime");
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查游戏时间配置！");
                    return;
                }

                if(config.exists(s+".MinPlayer")){
                    room.minPlayer = config.getInt(s+".MinPlayer");
                }else{
                    this.getLogger().info("房间【"+s+"】加载失败,请检查游戏时间配置！");
                    return;
                }
                room.roomName = s;
                Event.roomFinishPlayers.put(room,new ArrayList<>());
                Room.loadRoom(room);
                roomListHashMap.add(room);
                Event.placeBlocks.put(room,new ArrayList<>());
                Event.breakBlocks.put(room,new ArrayList<>());
                room.roomStatus = RoomStatus.ROOM_STATUS_WAIT;
                this.getLogger().info("房间【"+s+"】加载成功！");
            }
        }
    }
}
