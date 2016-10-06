package com.javasrc.statistics;

import javax.management.j2ee.statistics.*;
import java.util.*;

public abstract class AbstractStats implements Stats
{
    protected Map statistics = new TreeMap();

}
