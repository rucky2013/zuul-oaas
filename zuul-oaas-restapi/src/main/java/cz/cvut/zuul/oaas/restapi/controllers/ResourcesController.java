/*
 * The MIT License
 *
 * Copyright 2013-2014 Czech Technical University in Prague.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.cvut.zuul.oaas.restapi.controllers;

import cz.cvut.zuul.oaas.api.exceptions.ConflictException;
import cz.cvut.zuul.oaas.api.models.ResourceDTO;
import cz.cvut.zuul.oaas.api.services.ResourcesService;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * API for authorization server resource's management.
 */
@Controller
@RequestMapping("/v1/resources/")
public class ResourcesController {

    private static final String SELF_URI = "/v1/resources/";

    private @Setter ResourcesService resourceService;


    @RequestMapping(method = GET)
    public @ResponseBody List<ResourceDTO> getAllResources() {
        return resourceService.getAllResources();
    }

    @RequestMapping(value = "/public", method = GET)
    public @ResponseBody List<ResourceDTO> getAllPublicResources() {
        return resourceService.getAllPublicResources();
    }

    @ResponseBody
    @RequestMapping(value = "/{id}", method = GET)
    public ResourceDTO getResource(@PathVariable String id) {
        return resourceService.findResourceById(id);
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = POST)
    public void createResource(@RequestBody ResourceDTO resource, HttpServletResponse response) {
        String resourceId = resourceService.createResource(resource);

        // send redirect to URI of the created resource (i.e. api/resources/{id}/)
        response.setHeader("Location", SELF_URI + resourceId);
    }

    @ResponseStatus(NO_CONTENT)
    @RequestMapping(value = "/{resourceId}", method = PUT)
    public void updateResource(@PathVariable String resourceId, @RequestBody ResourceDTO resource) {
        if (! resourceId.equals(resource.getResourceId())) {
            throw new ConflictException("resourceId could not be changed");
        }
        resourceService.updateResource(resource);
    }

    @ResponseStatus(NO_CONTENT)
    @RequestMapping(value = "/{id}", method = DELETE)
    public void deleteResource(@PathVariable String id) {
        resourceService.deleteResourceById(id);
    }
}
