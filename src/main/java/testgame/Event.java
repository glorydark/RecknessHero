package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBookEnchanted;
import cn.nukkit.item.ItemEmerald;
import cn.nukkit.item.ItemTotem;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import gameapi.effect.Effect;
import gameapi.event.*;
import gameapi.room.Room;
import gameapi.room.RoomStatus;
import gameapi.scoreboard.UIScoreboard;
import gameapi.skill.CustomSkills;
import gameapi.sound.Sound;
import gameapi.utils.GameRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Event implements Listener {
    public static HashMap<Room, List<Player>> roomFinishPlayers = new HashMap<>();
    public static ConcurrentHashMap<Player, FormWindowSimple> playerFormWindowSimpleHashMap = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void touch(PlayerInteractEvent event){
        Room room = Room.getRoom("DRecknessHero", event.getPlayer());
        Player player = event.getPlayer();
        Item item = event.getItem();
        if(room == null){ return; }
        if(item instanceof ItemBookEnchanted && item.getCustomName().equals("§l§c退出房间")){
            room.removePlayer(player,true);
            player.sendMessage("§l§c您已退出房间！");
            return;
        }

        if(item instanceof ItemEmerald && item.getCustomName().equals("§l§a历史战绩")){
            Window.showPlayerHistoryWindow(player);
            return;
        }

        if(item instanceof ItemTotem && item.getCustomName().equals("§l§e选择职业")){
            try {
                Window.showPlayerSkillSelectWindow(player);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if(item.hasCompoundTag()){
            if(item.getNamedTag().contains("ItemType")){
                if(item.getNamedTag().getString("ItemType").equals("skillItem")){
                    Player p = event.getPlayer();
                    p.getInventory().setItem(2, Item.get(Block.AIR));
                    Effect effect = new Effect(cn.nukkit.potion.Effect.SPEED,2,2);
                    CustomSkills skill1 = new CustomSkills("加速","Speed Up",Item.get(Item.FEATHER), effect, true, 100);
                    p.sendMessage(TextFormat.GOLD+"您使用了技能:"+skill1.getCustomName());
                    cn.nukkit.potion.Effect effect1 = Effect.parseEffect(event.getPlayer(),effect);
                    effect1.setVisible(true);
                    p.addEffect(Effect.parseEffect(event.getPlayer(),effect));
                    Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                        @Override
                        public void onRun(int i) {
                            if(Room.getRoom(p) != null && Room.getRoom(p).getRoomStatus() == RoomStatus.ROOM_STATUS_GameStart) {
                                skill1.giveSkillItem(event.getPlayer(), false);
                                //p.removeEffect(skill1.getEffect().getId());
                                p.sendMessage(TextFormat.GREEN + "技能冷却结束！");
                            }
                            this.onCancel();
                        }
                    },skill1.getCoolDownTick());
                }
            }
        }

        if(event.getBlock().getId() == Block.EMERALD_BLOCK && room.getRoomStatus() == RoomStatus.ROOM_STATUS_GameStart) {
            if (!roomFinishPlayers.get(room).contains(event.getPlayer())) {
                roomFinishPlayers.get(room).add(event.getPlayer());
                int lastSec = room.getGameTime() - room.getTime();
                UIScoreboard.drawScoreBoardEntry(event.getPlayer(),MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),MainClass.getScoreboardSetting("rank_format").replace("%rank%", String.valueOf(roomFinishPlayers.get(room).indexOf(event.getPlayer())+1)),MainClass.getScoreboardSetting("time_format").replace("%time%",UIScoreboard.secToTime(lastSec)));
                if (room.getTime() < room.getGameTime() - 15) {
                    room.setTime(room.getGameTime() - 15);
                }
                for (Player p : room.getPlayers()) {
                    p.sendMessage(TextFormat.LIGHT_PURPLE+"%s 到达终点！".replace("%s", event.getPlayer().getName()));
                }
                /*
                switch (roomFinishPlayers.get(room).size()){
                    case 1:
                        if (room.getTime() < room.getGameTime() - 15) {
                            room.setTime(room.getGameTime() - 15);
                            for (Player p : room.getPlayers()) {
                                p.sendMessage("%s 玩家获得冠军！".replace("%s", event.getPlayer().getName()));
                                UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),"\uE176 Rank: 1st");
                            }
                        }
                        break;
                    case 2:
                        for (Player p : room.getPlayers()) {
                            p.sendMessage("%s 玩家获得亚军！".replace("%s", event.getPlayer().getName()));
                            UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),"\uE176 Rank: 2nd");
                        }
                        break;
                    case 3:
                        for (Player p : room.getPlayers()) {
                            p.sendMessage("%s 玩家获得季军！".replace("%s", event.getPlayer().getName()));
                            UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),"\uE176 Rank: 3rd");
                        }
                        break;
                    default:
                        for (Player p : room.getPlayers()) {
                            p.sendMessage("%s 玩家到达终点！".replace("%s", event.getPlayer().getName()));
                            UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),"\uE176 Rank: "+(roomFinishPlayers.get(room).indexOf(event.getPlayer())+1)+"th");
                        }
                        break;
                }

                 */
            }
        }
    }


    @EventHandler
    public void breakBlocks(BlockBreakEvent event){
        Room room = Room.getRoom("DRecknessHero", event.getPlayer());
        if(room == null){return;}
        if(room.getRoomStatus() != RoomStatus.ROOM_STATUS_GameStart){ return; }
        List<Effect> effectList = MainClass.getBlockAddonsInit(event.getBlock());
        if(effectList != null){
            for(Effect effect:effectList){
                cn.nukkit.potion.Effect effect1 = Effect.parseEffect(event.getPlayer(),effect);
                effect1.setVisible(true);
                event.getPlayer().addEffect(Effect.parseEffect(event.getPlayer(),effect));
                event.getPlayer().sendMessage("获得"+effect1.getName()+"*"+effect1.getAmplifier()+"*"+effect1.getDuration()/20+"秒");
                /*
                Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                    @Override
                    public void onRun(int i) {
                        event.getPlayer().removeEffect(effect1.getId());
                        this.onCancel();
                    }
                },effect1.getDuration());
                
                 */
            }
        }
        if(event.getBlock().getId() == Block.RED_MUSHROOM_BLOCK){
            Item item = Item.get(Block.REDSTONE_BLOCK);
            item.setCount(5);
            event.getPlayer().getInventory().addItem(item);
            event.getPlayer().sendMessage("获得5个屏障方块！");
        }
        event.setDrops(new Item[0]);
        event.setDropExp(0);
    }

    @EventHandler
    public void RoomGameStartEvent(RoomGameStartEvent event){
        Room room = event.getRoom();
        if(!event.getRoom().getGameName().equals("DRecknessHero")){ return;}
        for(Player p:room.getPlayers()){
            Event.roomFinishPlayers.put(event.getRoom(),new ArrayList<>());
            Item pickaxe = Item.get(Item.DIAMOND_PICKAXE);
            pickaxe.setCount(1);
            pickaxe.setCustomName("英雄之镐");
            p.getInventory().setItem(0, pickaxe);
            Effect effect = new Effect(cn.nukkit.potion.Effect.SPEED,2,5);
            CustomSkills skill1 = new CustomSkills("加速","Speed Up",Item.get(Item.FEATHER), effect, true, 100);
            skill1.giveSkillItem(p);
            Sound.playResourcePackOggMusic(p, "game_begin");
        }
    }

    @EventHandler
    public void ceremony(RoomCeremonyEvent event){
        if(!event.getRoom().getGameName().equals("DRecknessHero")){ return;}
        List<Player> players = roomFinishPlayers.get(event.getRoom());
        Config config = new Config(MainClass.path+"/rooms.yml",Config.YAML);
        List<String> winnerCmds = config.getList(event.getRoom().getRoomName()+".WinCommands",new ArrayList());
        List<String> failCmds = config.getList(event.getRoom().getRoomName()+".FailComands",new ArrayList());
        for(Player p:event.getRoom().getPlayers()){
            if(players.contains(p)){
                GameRecord.addGameRecord("DRecknessHero",p.getName(), "winning",1);
                p.sendMessage("§l§e您已成功完成比赛 §l§a"+GameRecord.getGameRecord("DRecknessHero",p.getName(),"winning")+" §l§e次");
                for(String cmd:winnerCmds){
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),cmd.replace("{player}",p.getName()));
                }
            }else{
                GameRecord.addGameRecord("DRecknessHero",p.getName(), "failed",1);
                p.sendMessage("§l§e您未完成比赛 §l§c"+GameRecord.getGameRecord("DRecknessHero",p.getName(),"failed")+" §l§e次");
                for(String cmd:failCmds){
                    Server.getInstance().dispatchCommand(Server.getInstance().getConsoleSender(),cmd.replace("{player}",p.getName()));
                }
            }
        }
    }

    @EventHandler
    public void gameEnd(RoomGameEndEvent event){
        if(!event.getRoom().getGameName().equals("DRecknessHero")){ return;}
        for(Player p:event.getRoom().getPlayers()){
            if(roomFinishPlayers.get(event.getRoom()).contains(p)){
                p.sendTitle("比赛结束","恭喜您获得了第"+(roomFinishPlayers.get(event.getRoom()).indexOf(p)+1)+"名",10,20,10);
                //UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),MainClass.getScoreboardSetting("rank_format").replace("%rank%", String.valueOf(roomFinishPlayers.get(event.getRoom()).indexOf(p)+1)));
                Sound.playResourcePackOggMusic(p,"winning");
            }else{
                p.sendTitle("比赛结束","您未完成比赛！",10,20,10);
                //UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),MainClass.getScoreboardSetting("failed_format"));
                Sound.playResourcePackOggMusic(p, "game_over");
            }
        }
    }

    @EventHandler
    public void end(RoomEndEvent event){
        if(!event.getRoom().getGameName().equals("DRecknessHero")){ return;}
        roomFinishPlayers.put(event.getRoom(),new ArrayList<>());
    }

    @EventHandler
    public void RoomGameProcessingListener(RoomGameProcessingListener event){
        Room room = event.getRoom();
        if(!room.getGameName().equals("DRecknessHero")){ return;}
        int lastSec = room.getGameTime() - room.getTime();
        for(Player p:room.getPlayers()){
            p.getFoodData().setLevel(20);
            if(lastSec == room.getGameTime()){
                if(roomFinishPlayers.get(event.getRoom()).contains(p)){
                    p.sendTitle("比赛结束","恭喜您获得了第"+(roomFinishPlayers.get(event.getRoom()).indexOf(p)+1)+"名",10,20,10);
                    UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),MainClass.getScoreboardSetting("rank_format").replace("%rank%", String.valueOf(roomFinishPlayers.get(event.getRoom()).indexOf(p)+1)));
                    Sound.playResourcePackOggMusic(p,"winning");
                }else{
                    p.sendTitle("比赛结束","您未完成比赛！",10,20,10);
                    UIScoreboard.drawScoreBoardEntry(p,MainClass.getScoreboardSetting("scoreboard_objective_name"),MainClass.getScoreboardSetting("scoreboard_display_name"),MainClass.getScoreboardSetting("failed_format"));
                    Sound.playResourcePackOggMusic(p, "game_over");
                }
            }else {
                if (!roomFinishPlayers.get(room).contains(p)) {
                    UIScoreboard.drawTimeBoardEntry(p, MainClass.getScoreboardSetting("scoreboard_objective_name"), MainClass.getScoreboardSetting("scoreboard_display_name"), MainClass.getScoreboardSetting("time_format").replace("%time%", UIScoreboard.secToTime(lastSec)));
                } else {
                    UIScoreboard.drawScoreBoardEntry(p, MainClass.getScoreboardSetting("scoreboard_objective_name"), MainClass.getScoreboardSetting("scoreboard_display_name"), MainClass.getScoreboardSetting("rank_format").replace("%rank%", String.valueOf(roomFinishPlayers.get(room).indexOf(p) + 1)), MainClass.getScoreboardSetting("time_format").replace("%time%", UIScoreboard.secToTime(lastSec)));
                }
            }
        }
    }

    @EventHandler
    public void GuiRespondedEvent(PlayerFormRespondedEvent event){
        if(event.getResponse() == null){ return; }
        if(Event.playerFormWindowSimpleHashMap.containsKey(event.getPlayer())){
            if(event.getWindow() != Event.playerFormWindowSimpleHashMap.get(event.getPlayer())){ return; }
            if(!(event.getWindow() instanceof FormWindowSimple)){ return; }
            String title = ((FormWindowSimple)event.getWindow()).getTitle();
            FormResponseSimple formResponseSimple = (FormResponseSimple) event.getResponse();
            Player player = event.getPlayer();
            switch (title){
                case "§l§e选择房间":
                    Room room = Room.getRoom("DRecknessHero", formResponseSimple.getClickedButton().getText());
                    MainClass.processJoin(room, player);
                    break;
                case "§l§e选择技能":
                    Room room1 = Room.getRoom(player);
                    if(room1 != null){
                        room1.setPlayerProperties(player,"skill1", formResponseSimple.getClickedButtonId());
                    }
                    break;
            }
        }
        playerFormWindowSimpleHashMap.remove(event.getPlayer());
    }
}
