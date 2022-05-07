package it.torkin;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionTools {
    
    private ReflectionTools(){}
    
    private static Logger logger = Logger.getLogger(ReflectionTools.class.getName());
    
    public static Method getMethodByRadix(String verb, String object, Class<?> c) throws NoSuchMethodException {
		
		Method[] methods = c.getMethods();
		String setAttrName = "Searching method " + verb + " for: " + object;
		logger.log(Level.INFO, setAttrName);
		for (int j = 0; j < methods.length; j ++) {
			if ((methods[j].getName().contains(verb)
					&& methods[j].getName().substring(verb.length()).compareTo(object.substring(0, 1).toUpperCase() + object.substring(1)) == 0
					&& !methods[j].isSynthetic())
					) {
				logger.log(Level.INFO, methods[j].getName());
				return methods[j];
			}
		}
		throw new NoSuchMethodException();
	}
}
