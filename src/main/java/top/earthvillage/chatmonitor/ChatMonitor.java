package top.earthvillage.chatmonitor;

import org.bukkit.BanList;
import org.bukkit.Bukkit;

import org.bukkit.command.CommandExecutor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatMonitor extends JavaPlugin implements Listener, CommandExecutor {
    private FileConfiguration config;


    @Override
    public void onEnable() {
        // 创建并加载配置文件
        this.saveDefaultConfig();
        config = this.getConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        //写注释最好的办法就是把注释塞进代码里yeeee
        Player 玩家 = event.getPlayer();
        String 聊天文本 = event.getMessage();
        //从配置文件获取违禁词列表
        List<String> bannedWords = config.getStringList("违禁词");
        // 检查玩家是否为OP或拥有特定权限
        if (玩家.isOp() || 玩家.hasPermission("内容审查豁免")) {
            return; // 管理员和拥有豁免权限的玩家不受影响
        }
        for (String bannedWord : bannedWords) {
            if (聊天文本.toLowerCase().contains(bannedWord.toLowerCase())) {
                event.setCancelled(true);
                玩家.sendMessage("§c讨论违规内容");

                // 获取封禁时长（分钟）
                int 惩罚时长 = config.getInt("惩罚时长", 5);

                // 执行临时封禁
                long banEndTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(惩罚时长);
                Bukkit.getBanList(BanList.Type.NAME).addBan(玩家.getName(), "使用禁用词语", new java.util.Date(banEndTime), null);

                // 踢出玩家
                玩家.kickPlayer("讨论违禁内容，已被封禁" + 惩罚时长 + "分钟。");
                break;
            }
        }
    }
/*
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
    */
}
