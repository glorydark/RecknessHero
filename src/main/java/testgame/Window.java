package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.network.protocol.ModalFormRequestPacket;
import cn.nukkit.utils.Config;
import com.sun.istack.internal.NotNull;
import gameapi.room.Room;
import gameapi.utils.GameRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.FileHandler;

public class Window {
    public static void showPlayerRoomListWindow(@NotNull Player player){
        if(!Event.playerFormWindowSimpleHashMap.contains(player)) {
            FormWindowSimple simple = new FormWindowSimple("§l§e选择房间", "请选择您的房间！");
            for (Room room : MainClass.roomListHashMap) {
                simple.addButton(new ElementButton(room.getRoomName()));
            }
            Event.playerFormWindowSimpleHashMap.put(player, simple);
            player.showFormWindow(simple);
        }
    }

    public static void showPlayerSkillSelectWindow(@NotNull Player player) throws IOException {
        if(!Event.playerFormWindowSimpleHashMap.contains(player)) {
            FormWindowSimple simple = new FormWindowSimple("§l§e选择技能", "请选择您的技能！");
            for (Room room : MainClass.roomListHashMap) {
                simple.addButton(new ElementButton("暴走达人"));
                simple.addButton(new ElementButton("建筑能手"));
                simple.addButton(new ElementButton("障碍达人"));
            }
            Event.playerFormWindowSimpleHashMap.put(player, simple);
            File file = new File("d:\\output.txt");
            try {
                FileWriter writer = new FileWriter(file, true);
                writer.write(simple.getJSONData());
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                ex.getMessage();
            }
        }
    }

    public static void showPlayerHistoryWindow(@NotNull Player player){
        if(!Event.playerFormWindowSimpleHashMap.contains(player)) {
            FormWindowSimple window = new FormWindowSimple("§l§a历史战绩", "");
            window.setContent("Win(获胜次数): §l§6" + GameRecord.getGameRecord("DRecknessHero", player.getName(), "winning") + "\n§fFail(获胜次数): §l§c" + GameRecord.getGameRecord("DRecknessHero", player.getName(), "failed"));
            player.showFormWindow(window);
        }
    }

    public static void showTestWindow(@NotNull Player player){
        ModalFormRequestPacket packet = new ModalFormRequestPacket();
        packet.data = "";
        packet.formId = ThreadLocalRandom.current().nextInt();
    }
}
