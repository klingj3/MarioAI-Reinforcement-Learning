package ch.idsia.utils.wox.serial;

import org.jdom.Element;

public interface ObjectWriter extends Serial
{
    Element write(Object o);
}
