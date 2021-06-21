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
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPickaxeDiamond;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import gameapi.effect.Effect;
import gameapi.event.RoomEndEvent;
import gameapi.event.RoomGameEndEvent;
import gameapi.event.RoomGameProcessingListener;
import gameapi.event.RoomGameStartEvent;
import gameapi.inventory.Inventory;
import gameapi.room.Room;
import gameapi.room.RoomStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Event implements Listener {
    public static HashMap<Room, List<Player>> roomFinishPlayers = new HashMap<>();
    public static HashMap<Player, FormWindowSimple> playerFormWindowSimpleHashMap = new HashMap<>();
    public static HashMap<Room, List<Block>> breakBlocks = new HashMap<>();
    public static HashMap<Room, List<Block>> placeBlocks = new HashMap<>();

    @EventHandler
    public void touch(PlayerInteractEvent event){
        Room room = Room.getRoom(event.getPlayer());
        if(room == null){ return; }
        if(event.getBlock().getId() == Block.EMERALD_BLOCK && room.roomStatus == RoomStatus.ROOM_STATUS_GameStart) {
            if (!roomFinishPlayers.get(room).contains(event.getPlayer())) {
                roomFinishPlayers.get(room).add(event.getPlayer());
                switch (roomFinishPlayers.get(room).size()){
                    case 1:
                        if (room.time < room.gameTime - 15) {
                            room.time = room.gameTime - 15;
                            for (Player p : room.players) {
                                p.sendTip("%s 玩家获得冠军！".replace("%s", event.getPlayer().getName()));
                            }
                        }
                        break;
                    case 2:
                        for (Player p : room.players) {
                            p.sendTip("%s 玩家获得亚军！".replace("%s", event.getPlayer().getName()));
                        }
                        break;
                    case 3:
                        for (Player p : room.players) {
                            p.sendTip("%s 玩家获得季军！".replace("%s", event.getPlayer().getName()));
                        }
                        break;
                    default:
                        for (Player p : room.players) {
                            p.sendTip("%s 玩家到达终点！".replace("%s", event.getPlayer().getName()));
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
                event.getPlayer().sendTip("获得"+effect1.getName()+"*"+effect1.getAmplifier()+"*"+effect1.getDuration()/20+"秒");
                Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                    @Override
                    public void onRun(int i) {
                        effect1.setAmplifier(0);
                        this.onCancel();
                    }
                },effect1.getDuration());
            }
        }
        if(event.getBlock().getId() == Block.RED_MUSHROOM_BLOCK){
            Item item = Item.get(Block.REDSTONE_BLOCK);
            item.setCount(5);
            event.getPlayer().getInventory().addItem(item);
            event.getPlayer().sendTip("获得5个屏障方块！");
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
    public void RoomGameProcessingListener(RoomGameProcessingListener event){
        for(Player p:event.getRoom().players){
            if(event.getRoom().time <= 0){return;}
            p.sendActionBar("还有 %d 秒比赛结束".replace("%d",String.valueOf(event.getRoom().gameTime-event.getRoom().time)));
        }
    }

    @EventHandler
    public void RoomGameStartEvent(RoomGameStartEvent event){
        Room room = event.getRoom();
        for(Player p:room.players){
            Event.roomFinishPlayers.put(event.getRoom(),new ArrayList<>());
            breakBlocks.put(event.getRoom(),new ArrayList<>());
            placeBlocks.put(event.getRoom(),new ArrayList<>());
            p.teleportImmediate(new Location(room.startSpawn.x,room.startSpawn.y,room.startSpawn.z,room.startSpawn.level));
        }
    }

    @EventHandler
    public void gameend(RoomGameEndEvent event){
        for(Player p:event.getRoom().players){
            if(roomFinishPlayers.get(event.getRoom()).contains(p)){
                p.sendTitle("比赛结束","恭喜您获得了第"+(roomFinishPlayers.get(event.getRoom()).indexOf(p)+1)+"名",10,20,10);
            }else{
                p.sendTitle("比赛结束","您未完成比赛！",10,20,10);
            }
        }
        roomFinishPlayers.put(event.getRoom(),new ArrayList<>());
    }

    @EventHandler
    public void end(RoomEndEvent event){
        for(Player p:event.getRoom().players) {
            p.getInventory().clearAll();
            Inventory.saveBag(p);
            Position pos = Server.getInstance().getDefaultLevel().getSafeSpawn();
            p.teleportImmediate(new Location(pos.x,pos.y,pos.z,pos.level));
            //玩家先走
        }
        String levelName = event.getRoom().roomName + "&worldload";
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
        if(playerFormWindowSimpleHashMap.get(event.getPlayer()) != null){
            FormResponseSimple formResponseSimple = (FormResponseSimple) event.getResponse();
            playerFormWindowSimpleHashMap.remove(event.getPlayer());
            if(Room.getRoom(formResponseSimple.getClickedButton().getText()) != null){
                if(Server.getInstance().isLevelLoaded(Room.getRoom(formResponseSimple.getClickedButton().getText()).roomName + "&worldload")) {
                    if(Room.getRoom(formResponseSimple.getClickedButton().getText()).roomStatus == RoomStatus.ROOM_STATUS_WAIT) {
                        Room room = Room.getRoom(formResponseSimple.getClickedButton().getText());
                        if(Room.addRoomPlayer(room, event.getPlayer())) {
                            event.getPlayer().teleportImmediate(new Location(room.waitSpawn.x, room.waitSpawn.y, room.waitSpawn.z, room.waitSpawn.level));
                            Inventory.saveBag(event.getPlayer());
                            event.getPlayer().getInventory().clearAll();
                            event.getPlayer().getInventory().addItem(new ItemPickaxeDiamond());
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
    }

    //内置简单反作弊
    @EventHandler
    public void FlyEvent(PlayerToggleFlightEvent event){
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerTeleportEvent(PlayerTeleportEvent event){
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
        if(!event.getPlayer().isOp()){
            event.setCancelled(true);
        }
    }
}
