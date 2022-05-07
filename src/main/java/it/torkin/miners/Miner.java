package it.torkin.miners;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.torkin.ReflectionTools;

/** Can mine all its accepted features.
 * Its subclasses must implement a mineX(String resourceName, Release release, Observation observation) method, where X is the feature to measure.
 * This method must mine measures of feature X for given resource at given release and store measurings in given observation. 
 */
public abstract class Miner {
    
    /** base name of all miner methods */
    protected static final String MINE_METHOD_VERB = "mine";
    
    /**needed to list subclasses miner methods */
    private Class<? extends Miner> actualClass;

    /**Each subclass must declare what Features can mine */
    public final Feature[] acceptedFeatures;

    
    private final Logger logger = Logger.getLogger(Miner.class.getName());
    
    protected Miner(Class<? extends Miner> actualClass, Feature... acceptedFeatures){

        this.actualClass = actualClass;
        this.acceptedFeatures = acceptedFeatures;
    }
    
    /**
     * mines features specified in bean for a given resource in a given release.
     * @param bean
     */
    public void mineData(MineDataBean bean) {
        
        Set<Feature> intersection = EnumSet.allOf(Feature.class);
        intersection.retainAll(Arrays.asList(acceptedFeatures));

        for (Feature f : intersection){
            try {
                ReflectionTools
                    .getMethodByRadix(MINE_METHOD_VERB, f.toString(), actualClass)    // miner method corresponding to requested and supported feature
                    .invoke(
                        this,
                        bean.getRelease(),                                          // target release
                        bean.getResourceName(),                                     // target resource name
                        bean
                            .getObservationMatrix()
                            .getMatrix()
                            .get(bean.getRelease().getName())
                            .get(bean.getResourceName())                            // Observation object which will hold target measures
                            );
            } catch (NoSuchMethodException e) {
                String msg = String.format("there is no implementation registered to mine feature %s", f.toString());
                logger.log(Level.WARNING, msg, e);
            } catch (IllegalAccessException  | IllegalArgumentException | InvocationTargetException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } 
        }
    }
}
