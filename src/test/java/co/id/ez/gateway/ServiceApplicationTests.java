package co.id.ez.gateway;

import co.id.ez.system.core.etc.Utility;
import co.id.ez.system.service.FutureService;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceApplicationTests extends FutureService{
    
    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, InterruptedException {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<FutureService> list = new ArrayList<>();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().startsWith("co.id.ez")) {
                final Class clazz = info.load();
//                Utility.printMessage(" Class Name " + clazz.getName());
                if (clazz.getSuperclass() == FutureService.class) {
                    Utility.printMessage(" - adding: " + clazz.getName());
                    list.add((FutureService) clazz.newInstance());
                }
            }
        }
        
        for (FutureService class1 : list) {
            Utility.printMessage(" Starting: " + class1.getName());
            class1.start();
        }
        
        Thread.sleep(30000);
        
        for (FutureService class1 : list) {
            Utility.printMessage(" Stoping: " + class1.getName());
            class1.stop();
        }
    }

    @Override
    public void run() {
        while (true) {            
            try {
                System.out.println("Service started.... ");
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                
            }
        }
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
