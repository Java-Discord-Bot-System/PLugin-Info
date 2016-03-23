package com.almightyalpaca.discord.bot.plugin.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.almightyalpaca.discord.bot.system.command.AbstractCommand;
import com.almightyalpaca.discord.bot.system.command.annotation.Command;
import com.almightyalpaca.discord.bot.system.events.CommandEvent;
import com.almightyalpaca.discord.bot.system.exception.PluginLoadingException;
import com.almightyalpaca.discord.bot.system.exception.PluginUnloadingException;
import com.almightyalpaca.discord.bot.system.plugins.Plugin;
import com.almightyalpaca.discord.bot.system.plugins.PluginInfo;
import com.almightyalpaca.discord.bot.system.util.StringUtils;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.MessageBuilder.Formatting;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;

public class InfoPlugin extends Plugin {

	class AvatarCommand extends AbstractCommand {

		public AvatarCommand() {
			super("avatar", "Stalk other user's avatar", "[user]");
		}

		@Command(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event) {
			final MessageBuilder builder = new MessageBuilder();
			builder.appendString("Your", Formatting.BOLD);
			builder.appendString(" avatar: " + event.getAuthor().getAvatarUrl());
			event.sendMessage(builder.build());
		}

		@Command(dm = true, guild = true, priority = 1, async = true)
		private void onCommand(final CommandEvent event, final User user) {
			final MessageBuilder builder = new MessageBuilder();
			builder.appendString(user.getUsername(), Formatting.BOLD);
			builder.appendString("'s avatar: " + user.getAvatarUrl());
			event.sendMessage(builder.build());
		}
	}

	private static class FormatToken {
		public final String	format;
		public final int	start;

		public FormatToken(final String format, final int start) {
			this.format = format;
			this.start = start;
		}
	}

	class IDCommand extends AbstractCommand {

		public IDCommand() {
			super("id", "I want id's", "[user]");
		}

		@Command(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event) {
			final MessageBuilder builder = new MessageBuilder();
			builder.appendString("Your", Formatting.BOLD).appendString(" id is ").appendString(event.getAuthor().getId(), Formatting.BLOCK);
			event.sendMessage(builder.build());
		}

		@Command(dm = true, guild = true, priority = 1, async = true)
		private void onCommand(final CommandEvent event, final User user) {
			final MessageBuilder builder = new MessageBuilder();
			builder.appendString(user.getUsername(), Formatting.BOLD).appendString("'s id is ").appendString(user.getId(), Formatting.BLOCK);
			event.sendMessage(builder.build());
		}

	}

	class InfoCommand extends AbstractCommand {

		public InfoCommand() {
			super("info", "MORE INFORMATION!!!", "[user/channel/guild]");
		}

		private String getAFKChannelName(final Guild guild) {
			final VoiceChannel channel = guild.getVoiceChannels().stream().filter(c -> c.getId().equalsIgnoreCase(guild.getAfkChannelId())).findAny().get();
			return channel == null ? null : channel.getName();
		}

		@Command(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event) {
			this.onCommand(event, event.getAuthor());
		}

		@Command(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event, final Guild guild) {
			final MessageBuilder builder = new MessageBuilder();

			String info = "";

			info += "Name:            " + guild.getName() + "\n";
			info += "ID:              " + guild.getId() + "\n";
			info += "Location:        " + guild.getRegion().getName() + "\n";
			info += "Icon ID:         " + guild.getIconId() + "\n";
			info += "Icon URL:        <" + guild.getIconUrl() + ">\n";
			info += "Owner ID:        " + guild.getOwnerId() + "\n";
			info += "Owner Name:      " + event.getJDA().getUserById(guild.getOwnerId()).getUsername() + "\n";
			info += "Users:           " + guild.getUsers().size() + "\n";
			info += "Text Channels:   " + guild.getTextChannels().size() + "\n";
			info += "Voice Channels:  " + guild.getVoiceChannels().size();
			info += "AFK Channel:     " + this.getAFKChannelName(guild);
			info += "Public Channel:  " + guild.getPublicChannel().getName();
			info += "Roles:           " + guild.getRoles().size();

			builder.appendCodeBlock(info, "");

			event.sendMessage(builder.build());
		}

		@Command(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event, final TextChannel channel) {
			final MessageBuilder builder = new MessageBuilder();

			String info = "";

			info += "Name:       " + channel.getName() + "\n";
			info += "ID:         " + channel.getId() + "\n";
			info += "Position:   " + channel.getPosition() + "\n";

			final String topic = InfoPlugin.this.removeFormatting(channel.getTopic());

			if (!topic.contains("\n")) {
				info += "Topic:      " + topic + "\n";
			} else {
				info += "Topic:      ";
				for (final String string : topic.split("\n")) {
					info += string + "\n            ";
				}
			}

			info = StringUtils.replaceLast(info, "\n            ", "");

			builder.appendCodeBlock(info, "");

			event.sendMessage(builder.build());
		}

		@Command(dm = true, guild = true, async = true)
		private void onCommand(final CommandEvent event, final User user) {
			final MessageBuilder builder = new MessageBuilder();

			String info = "";

			info += "Username:       " + user.getUsername() + "\n";
			info += "Discriminator:  " + user.getDiscriminator() + "\n";
			info += "ID:             " + user.getId() + "\n";
			info += "Status:         " + user.getOnlineStatus() + "\n";
			info += "Game:           " + user.getCurrentGame() + "\n";
			info += "Avatar ID:      " + user.getAvatarId() + "\n";
			info += "Avatar URL:     <" + user.getAvatarUrl() + ">\n";

			builder.appendCodeBlock(info, "");

			event.sendMessage(builder.build());
		}

	}

	private static final PluginInfo INFO = new PluginInfo("com.almightyalpaca.discord.bot.plugin.info", "1.0.0", "Almighty Alpaca", "Info Plugin", "Info about everything!");

	public InfoPlugin() {
		super(InfoPlugin.INFO);
	}

	@Override
	public void load() throws PluginLoadingException {
		this.registerCommand(new InfoCommand());
		this.registerCommand(new IDCommand());
		this.registerCommand(new AvatarCommand());
	}

	public String removeFormatting(final String topic) {
		// all the formatting keys to keep track of
		final String[] keys = new String[] { "*", "_", "`", "~~" };

		// find all tokens (formatting strings described above)
		final TreeSet<FormatToken> tokens = new TreeSet<>((t1, t2) -> Integer.compare(t1.start, t2.start));
		for (final String key : keys) {
			final Matcher matcher = Pattern.compile(Pattern.quote(key)).matcher(topic);
			while (matcher.find()) {
				tokens.add(new FormatToken(key, matcher.start()));
			}
		}

		// iterate over all tokens, find all matching pairs, and add them to the list toRemove
		final Stack<FormatToken> stack = new Stack<>();
		final List<FormatToken> toRemove = new ArrayList<>();
		boolean inBlock = false;
		for (final FormatToken token : tokens) {
			if (stack.empty() || !stack.peek().format.equals(token.format) || stack.peek().start + token.format.length() == token.start) {
				// we are at opening tag
				if (!inBlock) {
					// we are outside of block -> handle normally
					if (token.format.equals("`")) {
						// block start... invalidate all previous tags
						stack.clear();
						inBlock = true;
					}
					stack.push(token);
				} else if (token.format.equals("`")) {
					// we are inside of a block -> handle only block tag
					stack.push(token);
				}
			} else if (!stack.empty()) {
				// we found a matching close-tag
				toRemove.add(stack.pop());
				toRemove.add(token);
				if (token.format.equals("`") && stack.empty()) {
					// close tag closed the block
					inBlock = false;
				}
			}
		}

		// sort tags to remove by their start-index and iteratively build the remaining string
		Collections.sort(toRemove, (t1, t2) -> Integer.compare(t1.start, t2.start));
		final StringBuilder out = new StringBuilder();
		int currIndex = 0;
		for (final FormatToken formatToken : toRemove) {
			if (currIndex < formatToken.start) {
				out.append(topic.substring(currIndex, formatToken.start));
			}
			currIndex = formatToken.start + formatToken.format.length();
		}
		if (currIndex < topic.length()) {
			out.append(topic.substring(currIndex));
		}
		// return the stripped text, escape all remaining formatting characters (did not have matching open/close before or were left/right of block
		return out.toString().replace("*", "\\*").replace("_", "\\_").replace("~", "\\~");
	}

	@Override
	public void unload() throws PluginUnloadingException {}
}
