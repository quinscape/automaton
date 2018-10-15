package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.runtime.controller.GraphQLController;
import de.quinscape.automaton.runtime.logic.AutomatonStandardLogic;
import graphql.GraphQL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Standard bean definitions for automaton applications.
 */
@Configuration
public class AutomatonConfiguration
{
    @Bean
    public AutomatonStandardLogic automatonStandardLogic()
    {
        return new AutomatonStandardLogic();
    }

    @Bean
    public GraphQLController graphQLController(GraphQL graphQL)
    {
        return new GraphQLController(
            graphQL
        );
    }
}
