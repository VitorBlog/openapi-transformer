package dev.vitorpaulo.transformer;

import dev.vitorpaulo.transformer.command.TransformCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
@RequiredArgsConstructor
public class OpenApiTransformerApplication implements CommandLineRunner, ExitCodeGenerator {

    private final CommandLine.IFactory factory;
    private final TransformCommand transformCommand;
    private int exitCode;

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(transformCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(OpenApiTransformerApplication.class, args)));
    }
}
