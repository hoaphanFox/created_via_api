/**
 * <p>Put all configuration classes in this package. All Spring beans must be configured using Java configuration
 * classes, except the following:</p>
 * <ul>
 * <li>MVC Controllers and their WebFlux counterparts.</li>
 * <li>Spring data repositories.</li>
 * </ul>
 * <p>Other annotations, like @Scheduled, @JmsListener, @KafkaListener, and the like must be used in
 * configuration classes as well. Exceptions:</p>
 * <ul>
 * <li>@Transactional</li>
 * </ul>
 */

package au.com.foxsports.changeme.config;