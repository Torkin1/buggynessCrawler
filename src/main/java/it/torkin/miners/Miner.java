package it.torkin.miners;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.torkin.ReflectionTools;

public abstract class Miner {
    
    protected final String mineMethodVerb;
    private Class<? extends Miner> actualClass;
    public final Feature[] acceptedFeatures;
    private final Logger logger = Logger.getLogger(Miner.class.getName());
    
    protected Miner(String mineMethodVerb, Class<? extends Miner> actualClass, Feature... acceptedFeatures){

        this.mineMethodVerb = mineMethodVerb;
        this.actualClass = actualClass;
        this.acceptedFeatures = acceptedFeatures;
    }
    
    public void mineData(MineDataBean bean) {
        
        // call all measures methods associated with accepted fatures listed in bean
        Set<Feature> intersection = EnumSet.allOf(Feature.class);
        intersection.retainAll(Arrays.asList(acceptedFeatures));

        for (Feature f : intersection){
            try {
                ReflectionTools.getMethodByRadix(mineMethodVerb, f.toString(), actualClass).invoke(this, bean.getObservation());
            } catch (NoSuchMethodException e) {
                String msg = String.format("there is no implementation registered to mine feature %s", f.toString());
                logger.log(Level.WARNING, msg, e);
            } catch (IllegalAccessException  | IllegalArgumentException | InvocationTargetException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } 
        }
    }
}
