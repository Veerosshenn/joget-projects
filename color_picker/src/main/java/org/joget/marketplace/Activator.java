// Enhanced version of the original Color Picker plugin by Joget
// Modified by: Rosshen

package org.joget.marketplace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    protected Collection<ServiceRegistration> registrationList;

    public Activator() {
    }

    public void start(BundleContext context) {
        this.registrationList = new ArrayList();
        this.registrationList.add(context.registerService(ColorPicker.class.getName(), new ColorPicker(), (Dictionary)null));
    }

    public void stop(BundleContext context) {
        Iterator var2 = this.registrationList.iterator();

        while(var2.hasNext()) {
            ServiceRegistration registration = (ServiceRegistration)var2.next();
            registration.unregister();
        }

    }
}