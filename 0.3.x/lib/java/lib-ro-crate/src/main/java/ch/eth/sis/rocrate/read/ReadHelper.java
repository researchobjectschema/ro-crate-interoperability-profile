package ch.eth.sis.rocrate.read;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ReadHelper
{
    private ReadHelper() {
    }

    static Path resolveSingleRootDir(File extractedRoot)
    {
        List<Path> children;
        try (Stream<Path> s = Files.list(extractedRoot.toPath()))
        {
            children = s.filter(p -> !isArchiveJunk(p.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        if (children.size() == 1 && Files.isDirectory(children.get(0)))
        {
            return children.get(0);
        }
        return extractedRoot.toPath();
    }

    private static boolean isArchiveJunk(String name)
    {
        return name.equals("__MACOSX") || name.equals(".DS_Store") || name.equals("Thumbs.db");
    }
}
