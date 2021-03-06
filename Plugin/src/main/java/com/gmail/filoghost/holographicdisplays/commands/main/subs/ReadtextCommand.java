/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gmail.filoghost.holographicdisplays.commands.main.subs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.commands.Colors;
import com.gmail.filoghost.holographicdisplays.commands.CommandValidator;
import com.gmail.filoghost.holographicdisplays.commands.Strings;
import com.gmail.filoghost.holographicdisplays.commands.main.HologramSubCommand;
import com.gmail.filoghost.holographicdisplays.disk.HologramDatabase;
import com.gmail.filoghost.holographicdisplays.event.NamedHologramEditedEvent;
import com.gmail.filoghost.holographicdisplays.exception.CommandException;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.util.FileUtils;

public class ReadtextCommand extends HologramSubCommand {

	public ReadtextCommand() {
		super("readtext", "readlines");
		setPermission(Strings.BASE_PERM + "readtext");
	}

	@Override
	public String getPossibleArguments() {
		return "<hologramName> <fileWithExtension>";
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		NamedHologram hologram = CommandValidator.getNamedHologram(args[0]);
		
		try {
			String fileName = args[1];
			File targetFile = new File(HolographicDisplays.getInstance().getDataFolder(), fileName);
			CommandValidator.isTrue(FileUtils.isParentFolder(HolographicDisplays.getInstance().getDataFolder(), targetFile), "The file must be inside HolographicDisplays' folder.");
			CommandValidator.isTrue(!HolographicDisplays.isConfigFile(targetFile), "Cannot read default configuration files.");
			
			List<String> lines = FileUtils.readLines(targetFile);
			
			int linesAmount = lines.size();
			if (linesAmount > 40) {
				Strings.sendWarning(sender, "The file contained more than 40 lines, that have been limited.");
				linesAmount = 40;
			}
			
			hologram.clearLines();			
			for (int i = 0; i < linesAmount; i++) {
				hologram.getLinesUnsafe().add(HologramDatabase.deserializeHologramLine(lines.get(i), hologram));
			}
			hologram.refreshAll();

			HologramDatabase.saveHologram(hologram);
			HologramDatabase.trySaveToDisk();
			
			if (args[1].contains(".")) {
				if (isImageExtension(args[1].substring(args[1].lastIndexOf('.') + 1))) {
					Strings.sendWarning(sender, "The read file has an image's extension. If it is an image, you should use /" + label + " readimage.");
				}
			}
			
			sender.sendMessage(Colors.PRIMARY + "The lines were pasted into the hologram!");
			Bukkit.getPluginManager().callEvent(new NamedHologramEditedEvent(hologram));
			
		} catch (CommandException e) {
			throw e;
		} catch (FileNotFoundException e) {
			throw new CommandException("A file named '" + args[1] + "' doesn't exist in the plugin's folder.");
		} catch (IOException e) {
			throw new CommandException("I/O exception while reading the file. Is it in use?");
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommandException("Unhandled exception while reading the file! Please look the console.");
		}
	}
	
	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Reads the lines from a text file. Tutorial:",
			"1) Create a new text file in the plugin's folder",
			"2) Do not use spaces in the name",
			"3) Each line will be a line in the hologram",
			"4) Do /holograms readlines <hologramName> <fileWithExtension>",
			"",
			"Example: you have a file named 'info.txt', and you want",
			"to paste it in the hologram named 'test'. In this case you",
			"would execute "+ ChatColor.YELLOW + "/holograms readlines test info.txt");
	}
	
	@Override
	public SubCommandType getType() {
		return SubCommandType.EDIT_LINES;
	}

	private boolean isImageExtension(String input) {
		return Arrays.asList("jpg", "png", "jpeg", "gif").contains(input.toLowerCase());
	}
	
}
