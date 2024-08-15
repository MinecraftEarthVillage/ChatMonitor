package top.earthvillage.chatmonitor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ChatMonitor extends JavaPlugin implements Listener, CommandExecutor {
    private FileConfiguration config;
    public boolean debug;

    @Override
    public void onEnable() {
        // 创建并加载配置文件
        this.saveDefaultConfig();
        debug= getConfig().getBoolean("debug",false);
        config = this.getConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        // 注册 reloadchatmonitor 指令
        this.getCommand("reloadchatmonitor").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reloadchatmonitor")) {
            // 检查是否为OP管理员
            if (sender.isOp()) {
                // 重新加载配置文件
                this.reloadConfig();
                config = this.getConfig();
                sender.sendMessage("§a配置已重新加载!");
                return true;
            } else {
                sender.sendMessage("§c你没有权限执行此命令！");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        // 移除消息中的空格和标点符号
        message = message.replaceAll("[\\s\\p{Punct}]", "").toLowerCase();
        // 从配置文件获取关键词与指令的映射关系
        for (String keyword : config.getConfigurationSection("关键词指令映射").getKeys(false)) {
            if (message.toLowerCase().contains(keyword.toLowerCase())) {
                // 获取是否取消事件的配置
                boolean 拦截玩家消息 = config.getBoolean("关键词指令映射." + keyword + ".拦截玩家消息", true);

                // 根据配置决定是否取消事件
                if (拦截玩家消息) {
                    event.setCancelled(true);
                }
                // 调试模式开启时发送警告信息
                if (debug) {
                    player.sendMessage("§6你触发了关键词：" + keyword);
                }

                // 获取对应指令
                List<String> commands = config.getStringList("关键词指令映射." + keyword + ".执行指令");

                // 执行指令
                for (String command : commands) {
                    // 将命令中的占位符替换为玩家名称
                    String parsedCommand = command.replace("{player}", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                }

                break;
            }
        }
    }
}
