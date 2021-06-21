package testgame;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import gameapi.room.Room;

public class Command extends cn.nukkit.command.Command {

    public Command(String name) {
        super(name,"§e暴走英雄","/drh");
    }

    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(commandSender instanceof Player) {
            if(Room.getRoom((Player) commandSender) != null){ return true; }
            FormWindowSimple simple = new FormWindowSimple("暴走英雄","请选择您的房间！");
            for(Room room:MainClass.roomListHashMap){
                simple.addButton(new ElementButton(room.roomName));
            }
            Event.playerFormWindowSimpleHashMap.put((Player) commandSender,simple);
            ((Player) commandSender).showFormWindow(simple);
        }
        return true;
    }
}
