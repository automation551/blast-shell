package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.runtime.shell.CommandSessionImpl;
import org.apache.felix.gogo.runtime.shell.CommandShellImpl;
import org.osgi.service.command.CommandSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Serves as a registry for commands -- it detects when command objects are loaded by Spring.
 */
public class CommandRegistry implements BeanPostProcessor {
    CommandShellImpl commandShell;

    Map<String, Action> commandRegistry = new HashMap<String, Action>();


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Action) {
            commandRegistry.put(beanName, (Action) bean);
        }

        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private String getName(Object bean) {

        if (bean.getClass().isAnnotationPresent(Command.class)) {
            Command command = bean.getClass().getAnnotation(Command.class);
            String scope = command.scope();
            String function = command.name();
            if (scope != null && function != null) {
                if (bean instanceof Action) {
                    commandShell.addCommand(scope, new SimpleSpringBeanCommand((Action) bean), function);
                } else {
                    commandShell.addCommand(scope, bean, function);
                }
                return scope + ":" + function;
            }
        }
        return null;
    }

    public CommandShellImpl getCommandShell() {
        return commandShell;
    }

    public void setCommandShell(CommandShellImpl commandShell) {
        this.commandShell = commandShell;
    }

    public void registerCommandsInSession(CommandSession session) {
        Set<String> commandNames = new HashSet<String>();
        for (Action action : commandRegistry.values()) {
            String name = getName(action);
            commandNames.add(name);
            session.put(name, new SimpleSpringBeanCommand(action));
        }
        session.put(CommandSessionImpl.COMMANDS, commandNames);

    }
}