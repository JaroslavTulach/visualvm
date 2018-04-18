package com.sun.tools.visualvm.sampler.cpu;

import com.sun.tools.visualvm.profiling.actions.ProfiledSourceSelection;
import com.sun.tools.visualvm.profiling.actions.ProfilerPopupCustomizer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ProfilerPopupCustomizer.class)
public final class CPUViewPopupCustomizer extends ProfilerPopupCustomizer {

    public CPUViewPopupCustomizer() {
    }

    @Override
    public JMenuItem[] getMenuItems(ProfiledSourceSelection app, View view, Mode mode) {
        List<JMenuItem> menu = new ArrayList<>();
        fill(app, menu);
        return menu.toArray(new JMenuItem[menu.size()]);
    }


    private static void fill(ProfiledSourceSelection app, final List<JMenuItem> menu) {
        List<? extends Action> actions = org.openide.util.Utilities.actionsForPath("VisualVM/CPUView");
        final InstanceContent ic = new InstanceContent();
        ic.add(app);
        ic.add(app.getApplication());
        AbstractLookup lookup = new AbstractLookup(ic);

        for (Action a : actions) {
            if (a instanceof org.openide.util.ContextAwareAction) {
                a = ((org.openide.util.ContextAwareAction) a).createContextAwareInstance(lookup);
            }
            JMenuItem b = new JMenuItem();
            Actions.connect(b, a);
            b.setText((String) a.getValue(Action.NAME));
            menu.add(b);
        }
    }

}
