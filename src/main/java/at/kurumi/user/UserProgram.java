package at.kurumi.user;

import at.kurumi.commands.Command;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProgram extends Command {

    private static final Logger LOG = LogManager.getLogger("User");

    private final UserSLO userSLO;

    private final Map<String, Runnable> operations = new HashMap<>();

    public UserProgram(UserSLO userSLO) {
        this.userSLO = userSLO;

        operations.put("hello", this::hello); // TODO sth like this?
    }

    @Override
    public String getName() {
        return "user";
    }

    @Override
    public String getDescription() {
        return "Manage your user profile";
    }

    @Override
    public List<ApplicationCommandOptionData> getOptions() {
        return List.of(
                super.optionData(
                        "operation",
                        "What operation to execute",
                        ApplicationCommandOption.Type.STRING.getValue(),
                        true
                ),
                super.optionData(
                        "argument 0",
                        "Optional argument",
                        ApplicationCommandOption.Type.STRING.getValue(),
                        false
                ));
    }

    @Override
    public void handle(ApplicationCommandInteractionEvent e) {

    }

    private void hello() {
        final var discordId = 11313131231L;
        final var name = "UserXX11";

        // TODO insert into db
    }
}
