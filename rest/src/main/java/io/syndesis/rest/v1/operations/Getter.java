/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.operations;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiParam;
import io.syndesis.dao.manager.WithDataManager;
import io.syndesis.model.WithId;

public interface Getter<T extends WithId> extends Resource<T>, WithDataManager {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    default T get(@PathParam("id") @ApiParam(required = true) String id) {
        Class<T> modelClass = (Class<T>) resourceKind().getModelClass();
        T result = getDataManager().fetch(modelClass, id);
        if( result == null ) {
            throw new EntityNotFoundException();
        }
        return result;
    }

}
