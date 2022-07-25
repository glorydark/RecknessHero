package testgame;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import com.sun.istack.internal.NotNull;
import gameapi.room.Room;
import gameapi.utils.GameRecord;

public class Window {
    public static void showPlayerRoomListWindow(@NotNull Player player){
        if(!Event.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple simple = new FormWindowSimple("§l§e选择房间", "请选择您的房间！");
            for (Room room : MainClass.roomListHashMap) {
                simple.addButton(new ElementButton(room.getRoomName()));
            }
            Event.playerFormWindowSimpleHashMap.put(player, simple);
            player.showFormWindow(simple);
        }
    }

    public static void showPlayerSkillSelectWindow(@NotNull Player player) {
        if(!Event.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple simple = new FormWindowSimple("§l§e选择技能", "请选择您的技能！");
            for(String string: MainClass.skills.keySet()){
                simple.addButton(new ElementButton(string));
            }
            Event.playerFormWindowSimpleHashMap.put(player, simple);
            player.showFormWindow(simple);
        }
    }

    public static void showPlayerHistoryWindow(@NotNull Player player){
        if(!Event.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple window = new FormWindowSimple("§l§a历史战绩", "");
            window.setContent("Win(获胜次数): §l§6" + GameRecord.getGameRecord("DRecknessHero", player.getName(), "win") + "\n§fFail(获胜次数): §l§c" + GameRecord.getGameRecord("DRecknessHero", player.getName(), "lose"));
            Event.playerFormWindowSimpleHashMap.put(player, window);
            player.showFormWindow(window);
        }
    }

    public static void showVoteForMap(@NotNull Player player){
        if(!Event.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple window = new FormWindowSimple("§l§e选择地图", "");
            for(String map: MainClass.maps){
                window.addButton(new ElementButton(map));
            }
            Event.playerFormWindowSimpleHashMap.put(player, window);
            player.showFormWindow(window);
        }
    }
}
