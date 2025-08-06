package tech.minediamond.vortex.service.factory;

import org.fxmisc.richtext.CodeArea;
import tech.minediamond.vortex.service.DynamicLineNumberFactory;

public interface DynamicLineNumberFactoryFactory {
    DynamicLineNumberFactory create(CodeArea codeArea);
}
