package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBookEnchanted;
import cn.nukkit.item.ItemPickaxeDiamond;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import gameapi.effect.Effect;
import gameapi.event.*;
import gameapi.inventory.Inventory;
import gameapi.room.Room;
import gameapi.room.RoomStatus;
import gameapi.sound.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Event implements Listener {
    public static ConcurrentHashMap<Room, List<Player>> roomFinishPlayers = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Player, FormWindowSimple> playerFormWindowSimpleHashMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Room, List<Block>> breakBlocks = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Room, List<Block>> placeBlocks = new ConcurrentHashMap<>();

    @EventHandler
    public void touch(PlayerInteractEvent event){
        Room room = Room.getRoom(event.getPlayer());
        if(room == null){ return; }
        if(event.getItem() instanceof ItemBookEnchanted && event.getItem().getCustomName().equals(TextFormat.BOLD+"退出房间")){
            room.getPlayers().remove(event.getPlayer());
            Position spawn = Server.getInstance().getDefaultLevel().getSafeSpawn();
            event.getPlayer().sendMessage("您已退出房间！");
            Inventory.loadBag(event.getPlayer());
            event.getPlayer().teleportImmediate(new Location(spawn.x,spawn.y,spawn.z,spawn.level));
            return;
        }
        if(event.getBlock().getId() == Block.EMERALD_BLOCK && room.getRoomStatus() == RoomStatus.ROOM_STATUS_GameStart) {
            if (!roomFinishPlayers.get(room).contains(event.getPlayer())) {
                roomFinishPlayers.get(room).add(event.getPlayer());
                switch (roomFinishPlayers.get(room).size()){
                    case 1:
                        if (room.getTime() < room.getRoundGameTime() - 15) {
                            room.setTime(room.getRoundGameTime() - 15);
                            for (Player p : room.getPlayers()) {
                                p.sendMessage("%s 玩家获得冠军！".replace("%s", event.getPlayer().getName()));
                            }
                        }
                        break;
                    case 2:
                        for (Player p : room.getPlayers()) {
                            p.sendMessage("%s 玩家获得亚军！".replace("%s", event.getPlayer().getName()));
                        }
                        break;
                    case 3:
                        for (Player p : room.getPlayers()) {
                            p.sendMessage("%s 玩家获得季军！".replace("%s", event.getPlayer().getName()));
                        }
                        break;
                    default:
                        for (Player p : room.getPlayers()) {
                            p.sendMessage("%s 玩家到达终点！".replace("%s", event.getPlayer().getName()));
                        }
                        break;
                }
            }
        }
    }


    @EventHandler
    public void breakBlocks(BlockBreakEvent event){
        if(Room.getRoom(event.getPlayer()) == null){return;}
        if(MainClass.getBlockAddonsInit(event.getBlock()) != null){
            for(Effect effect:MainClass.getBlockAddonsInit(event.getBlock())){
                cn.nukkit.potion.Effect effect1 = Effect.parseEffect(event.getPlayer(),effect);
                effect1.setVisible(true);
                event.getPlayer().addEffect(Effect.parseEffect(event.getPlayer(),effect));
                event.getPlayer().sendMessage("获得"+effect1.getName()+"*"+effect1.getAmplifier()+"*"+effect1.getDuration()/20+"秒");
                Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                    @Override
                    public void onRun(int i) {
                        event.getPlayer().removeEffect(effect1.getId());
                        this.onCancel();
                    }
                },effect1.getDuration());
            }
        }
        if(event.getBlock().getId() == Block.RED_MUSHROOM_BLOCK){
            Item item = Item.get(Block.REDSTONE_BLOCK);
            item.setCount(5);
            event.getPlayer().getInventory().addItem(item);
            event.getPlayer().sendMessage("获得5个屏障方块！");
        }
        breakBlocks.get(Room.getRoom(event.getPlayer())).add(event.getBlock());
        event.setDrops(new Item[0]);
        event.setDropExp(0);
    }

    @EventHandler
    public void placeBlocks(BlockPlaceEvent event){
        if(Room.getRoom(event.getPlayer()) == null){return;}
        placeBlocks.get(Room.getRoom(event.getPlayer())).add(event.getBlock());
    }

    @EventHandler
    public void RoomReadyStartEvent(RoomReadyStartEvent event){
        Room room = event.getRoom();
        for(Player p:room.getPlayers()){
            p.getInventory().clearAll();
        }
    }

    @EventHandler
    public void RoomGameStartEvent(RoomGameStartEvent event){
        Room room = event.getRoom();
        for(Player p:room.getPlayers()){
            p.getInventory().clearAll();
            Event.roomFinishPlayers.put(event.getRoom(),new ArrayList<>());
            breakBlocks.put(event.getRoom(),new ArrayList<>());
            placeBlocks.put(event.getRoom(),new ArrayList<>());
            p.teleportImmediate(new Location(room.getStartLocation().x,room.getStartLocation().y,room.getStartLocation().z,room.getStartLocation().level));
            p.getInventory().addItem(new ItemPickaxeDiamond());
            Sound.playResourcePackOggMusic(p, "game_begin");
        }
    }

    @EventHandler
    public void ceremony(RoomCeremonyEvent event){
        List<Player> players = roomFinishPlayers.get(event.getRoom());
        Config config = new Config(MainClass.path+"/rooms.yml",Config.YAML);
        List<String> winnerCmds = config.getList(event.getRoom().getName()+".WinCommands",new ArrayList());
        List<String> failCmds = config.getList(event.getRoom().getName()+".FailComands",new ArrayList());
        for(Player p:event.getRoom().getPlayers()){
            if(players.contains(p)){
                for(String cmd:winnerCmds){
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),cmd.replace("{player}",p.getName()));
                }
            }else{
                for(String cmd:failCmds){
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),cmd.replace("{player}",p.getName()));
                }
            }
        }
    }

    @EventHandler
    public void gameend(RoomGameEndEvent event){
        for(Player p:event.getRoom().getPlayers()){
            if(roomFinishPlayers.get(event.getRoom()).contains(p)){
                p.sendTitle("比赛结束","恭喜您获得了第"+(roomFinishPlayers.get(event.getRoom()).indexOf(p)+1)+"名",10,20,10);
                Sound.playResourcePackOggMusic(p,"winning");
            }else{
                p.sendTitle("比赛结束","您未完成比赛！",10,20,10);
                Sound.playResourcePackOggMusic(p, "game_over");
            }
        }
        roomFinishPlayers.put(event.getRoom(),new ArrayList<>());
    }

    @EventHandler
    public void end(RoomEndEvent event){
        for(Player p:event.getRoom().getPlayers()) {
            p.getInventory().clearAll();
            Inventory.saveBag(p);
            Position pos = Server.getInstance().getDefaultLevel().getSafeSpawn();
            p.teleportImmediate(new Location(pos.x,pos.y,pos.z,pos.level));
            //玩家先走
        }
        String levelName = event.getRoom().getName() + "&worldload";
        Level level = Server.getInstance().getLevelByName(levelName);
        for(Block block:breakBlocks.get(event.getRoom())){
            level.setBlock(new Vector3(block.x,block.y,block.z),block);
        }
        for(Block block:placeBlocks.get(event.getRoom())){
            level.setBlock(new Vector3(block.x,block.y,block.z), Block.get(0));
        }
        breakBlocks.put(event.getRoom(),new ArrayList<>());
        placeBlocks.put(event.getRoom(),new ArrayList<>());
    }

    @EventHandler
    public void RoomGameProcessingListener(RoomGameProcessingListener event){
        Room room = event.getRoom();
        for(Player p:room.getPlayers()){
            if(roomFinishPlayers.get(room).contains(p)){
                p.sendActionBar("剩余时间:"+(room.getRoundGameTime() - room.getTime())+"秒！");
            }
        }
    }

    @EventHandler
    public void Quit(PlayerQuitEvent event){
        if(Room.getRoom(event.getPlayer()) != null){
            Inventory.saveBag(event.getPlayer());
            event.getPlayer().setPosition(Server.getInstance().getDefaultLevel().getSafeSpawn());
            playerFormWindowSimpleHashMap.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void GuiRespondedEvent(PlayerFormRespondedEvent event){
        if(event.getResponse() == null){ return; }
        if(Event.playerFormWindowSimpleHashMap.containsKey(event.getPlayer())){
            if(event.getWindow() != Event.playerFormWindowSimpleHashMap.get(event.getPlayer())){ return;}
            FormResponseSimple formResponseSimple = (FormResponseSimple) event.getResponse();
            if(Room.getRoom(formResponseSimple.getClickedButton().getText()) != null){
                if(Server.getInstance().isLevelLoaded(Room.getRoom(formResponseSimple.getClickedButton().getText()).getName() + "&worldload")) {
                    if(Room.getRoom(formResponseSimple.getClickedButton().getText()).getRoomStatus() == RoomStatus.ROOM_STATUS_WAIT) {
                        Room room = Room.getRoom(formResponseSimple.getClickedButton().getText());
                        if(room.addPlayers(event.getPlayer())) {
                            event.getPlayer().teleportImmediate(new Location(room.getWaitLocation().x, room.getWaitLocation().y, room.getWaitLocation().z, room.getWaitLocation().level));
                            Inventory.saveBag(event.getPlayer());
                            PlayerInventory inventory = event.getPlayer().getInventory();
                            event.getPlayer().getInventory().clearAll();
                            Item addItem = new ItemBookEnchanted();
                            addItem.setCustomName(TextFormat.BOLD+"退出房间");
                            event.getPlayer().getInventory().addItem(addItem);
                        }else{
                            event.getPlayer().sendMessage("房间人数已满！");
                        }
                    }else{
                        event.getPlayer().sendMessage("游戏已经开始！");
                    }
                }else{
                    event.getPlayer().sendMessage("地图未加载完毕！");
                }
            }else{
                event.getPlayer().sendMessage("该房间不存在！");
            }
        }
        playerFormWindowSimpleHashMap.remove(event.getPlayer());
    }

    //内置简单反作弊
    @EventHandler
    public void FlyEvent(PlayerToggleFlightEvent event){
        if(Room.getRoom(event.getPlayer()) == null){ return; }
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerTeleportEvent(PlayerTeleportEvent event){
        if(Room.getRoom(event.getPlayer()) == null){ return; }
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
        if(Room.getRoom(event.getPlayer()) == null){ return; }
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerInvalidMoveEvent(PlayerInvalidMoveEvent event){
        if(Room.getRoom(event.getPlayer()) == null){ return; }
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }
}
