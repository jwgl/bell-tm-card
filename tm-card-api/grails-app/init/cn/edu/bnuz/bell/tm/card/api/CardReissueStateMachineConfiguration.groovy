package cn.edu.bnuz.bell.tm.card.api

import cn.edu.bnuz.bell.workflow.Events
import cn.edu.bnuz.bell.workflow.States
import cn.edu.bnuz.bell.workflow.actions.AutoEntryAction
import cn.edu.bnuz.bell.workflow.config.StandardActionConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.statemachine.action.Action
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer

@Configuration
@EnableStateMachine(name='CardReissueFormStateMachine')
@Import(StandardActionConfiguration)
class CardReissueStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<States, Events> {
    @Autowired
    StandardActionConfiguration actions

    @Override
    void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states
            .withStates()
                .initial(States.CREATED)
                .state(States.CREATED,   [actions.logEntryAction()], null)
                .state(States.COMMITTED, [actions.logEntryAction(), actions.committedEntryAction()], [actions.workitemProcessedAction()])
                .state(States.CHECKED,   [actions.logEntryAction(), checkedEntryAction()], null)
                .state(States.REJECTED,  [actions.logEntryAction(), actions.rejectedEntryAction()],  [actions.workitemProcessedAction()])
                .state(States.PROGRESS,  [actions.logEntryAction(), progressEntryAction()], null)
                .state(States.FINISHED,  [actions.logEntryAction(), actions.notifyCommitterAction()], null)
    }

    @Override
    void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
            .withInternal()
                .source(States.CREATED)
                .event(Events.UPDATE)
                .action(actions.logTransitionAction())
                .and()
            .withExternal()
                .source(States.CREATED)
                .event(Events.COMMIT)
                .target(States.COMMITTED)
                .and()
            .withExternal()
                .source(States.COMMITTED)
                .event(Events.ACCEPT)
                .target(States.CHECKED)
                .and()
            .withExternal()
                .source(States.COMMITTED)
                .event(Events.REJECT)
                .target(States.REJECTED)
                .and()
            .withExternal()
                .source(States.CHECKED)
                .event(Events.ACCEPT)
                .target(States.PROGRESS)
                .and()
            .withExternal()
                .source(States.PROGRESS)
                .event(Events.ACCEPT)
                .target(States.FINISHED)
                .and()
            .withExternal()
                .source(States.PROGRESS)
                .event(Events.REJECT)
                .target(States.CHECKED)
                .and()
            .withInternal()
                .source(States.REJECTED)
                .event(Events.UPDATE)
                .action(actions.logTransitionAction())
                .and()
            .withExternal()
                .source(States.REJECTED)
                .event(Events.COMMIT)
                .target(States.COMMITTED)
    }

    @Bean
    Action<States, Events> checkedEntryAction() {
        new AutoEntryAction()
    }

    @Bean
    Action<States, Events> progressEntryAction() {
        new AutoEntryAction()
    }
}
