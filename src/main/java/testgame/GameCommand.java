package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import com.sun.istack.internal.NotNull;
import gameapi.room.Room;

public class GameCommand extends Command {
    public GameCommand(String name) {
        super(name,"暴走英雄","/drh");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (Server.getInstance().getPlayer(sender.getName()) != null) {
            showPlayerRoomListWindow((Player) sender);
        } else {
            sender.sendMessage("请在游戏内使用！");
        }
        return true;
    }

    public void showPlayerRoomListWindow(@NotNull Player player){
        FormWindowSimple simple = new FormWindowSimple("暴走英雄", "请选择您的房间！");
        for (Room room : MainClass.roomListHashMap) {
            simple.addButton(new ElementButton(room.roomName));
        }
        Event.playerFormWindowSimpleHashMap.put(player, simple);
        player.showFormWindow(simple);
    }
}
