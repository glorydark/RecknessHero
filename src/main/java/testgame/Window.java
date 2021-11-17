package testgame;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import com.sun.istack.internal.NotNull;
import gameapi.room.Room;

public class Window {
    public static void showPlayerRoomListWindow(@NotNull Player player){
        FormWindowSimple simple = new FormWindowSimple("暴走英雄", "请选择您的房间！");
        for (Room room : MainClass.roomListHashMap) {
            simple.addButton(new ElementButton(room.getRoomName()));
        }
        Event.playerFormWindowSimpleHashMap.put(player, simple);
        player.showFormWindow(simple);
    }

    public static void showPlayerSkillSelectWindow(@NotNull Player player){
        FormWindowSimple simple = new FormWindowSimple("暴走英雄", "请选择您的默认技能！");
        for (Room room : MainClass.roomListHashMap) {
            simple.addButton(new ElementButton("速"));
        }
        Event.playerFormWindowSimpleHashMap.put(player, simple);
        player.showFormWindow(simple);
    }
}
