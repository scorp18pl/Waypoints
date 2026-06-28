package org.scorp.waypoints;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.scorp.waypoints.Waypoint.WaypointBadWorldException;
import org.scorp.waypoints.Waypoint.WaypointNameExistsException;
import org.scorp.waypoints.Waypoint.WaypointNotFoundException;
import org.scorp.waypoints.command.*;
import org.scorp.waypoints.command.subcommands.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WaypointCommand implements CommandExecutor, TabExecutor
{
  static List<SubCommand> subCommands =
      List.of(new AddSubCommand(), new CoordsSubCommand(), new ListSubCommand(),
          new RemoveSubCommand(), new RenameSubCommand(),
          new SetTypeSubCommand(),
          new ShareSubCommand(), new PointSubCommand());

  private static SubCommand getSubCommandWithName(String name)
  {
    for (SubCommand sc : subCommands)
    {
      if (sc.getName().equals(name))
      {
        return sc;
      }
    }

    return null;
  }

  private static String getUsageString(String usageBody)
  {
    return "Usage:\n" + usageBody;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender,
                           @NotNull Command command, @NotNull String label,
                           @NotNull String[] args)
  {
    if (args.length == 0)
    {
      return false;
    }

    String subCommandName = args[0];
    SubCommand subCommand = getSubCommandWithName(subCommandName);
    int argCount = args.length - 1; // skip subcommand from args
    if (subCommand == null)
    {
      return false;
    }

    try
    {
      if (!subCommand.getPossibleArgCounts().contains(argCount))
      {
        throw new InvalidCommandException();
      }

      String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);

      for (String arg : subCommandArgs)
      {
        if (!CommandUtils.isNameValid(arg))
        {
          throw new IllegalCharactersException(arg);
        }
      }

      subCommand.onCommand(sender, subCommandArgs);
    } catch (InvalidCommandException e)
    {
      sender.sendMessage(Utils.getErrorString(e.getMessage()));
      sender.sendMessage(
          Utils.getInfoString(getUsageString(subCommand.getUsageMessage())));
    } catch (WaypointNotFoundException | WaypointNameExistsException | WaypointBadWorldException e)
    {
      sender.sendMessage(Utils.getErrorString(e.getMessage()));
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                              @NotNull Command command,
                                              @NotNull String label,
                                              @NotNull String[] args)
  {
    if (args.length == 1)
    {
      return subCommands.stream().map(
              SubCommand::getName)
          .collect(Collectors.toCollection(ArrayList::new));
    }

    String subCommandName = args[0];
    SubCommand subCommand = getSubCommandWithName(subCommandName);
    int argCount = args.length - 1; // skip subcommand from args
    if (subCommand == null ||
        subCommand.getPossibleArgCounts().stream()
            .max(Comparator.naturalOrder()).orElse(0) < argCount
    )
    {
      return List.of();
    }

    try
    {
      return subCommand.onTabComplete(sender, argCount - 1);
    } catch (PlayerOnlyCommandException e)
    {
      return List.of();
    }
  }
}
