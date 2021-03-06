package de.ids_mannheim.korap.web.service.full;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sun.jersey.spi.container.ResourceFilters;

import de.ids_mannheim.korap.dao.ResourceDao;
import de.ids_mannheim.korap.dto.ResourceDto;
import de.ids_mannheim.korap.dto.converter.ResourceConverter;
import de.ids_mannheim.korap.entity.Resource;
import de.ids_mannheim.korap.utils.JsonUtils;
import de.ids_mannheim.korap.web.filter.PiwikFilter;

/**
 * Provides information about free resources.
 * 
 * @author margaretha
 *
 */
@Controller
@Path("resource/")
@ResourceFilters({PiwikFilter.class})
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ResourceService {

    private static Logger jlog = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private ResourceConverter resourceConverter;


    @GET
    @Path("info")
    public Response getAllResourceInfo () {
        List<Resource> resources = resourceDao.getAllResources();
        List<ResourceDto> resourceDtos = resourceConverter
                .convertToResourcesDto(resources);
        String result = JsonUtils.toJSON(resourceDtos);
        jlog.debug("/info " + resourceDtos.toString());
        return Response.ok(result).build();
    }
}
