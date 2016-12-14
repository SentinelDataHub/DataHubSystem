/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2016 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.olingo.v1.operations;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.xmloutput.impl.Abbreviated;

import fr.gael.dhus.olingo.v1.ExpectedException;
import fr.gael.drbx.cortex.DrbCortexModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.Facets;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.FunctionImportParameter;
import org.apache.olingo.odata2.api.edm.provider.ReturnType;
import org.apache.olingo.odata2.api.exception.ODataException;

/**
 * The SPARQL service operation, can only execute SELECT queries.
 * <p>see the <a href="https://www.w3.org/TR/sparql11-protocol/">SPARQL 1.1 Protocol</a>.
 */
public class Sparql extends AbstractOperation
{
   public static String NAME = "SparQL";

   @Override
   public String getName()
   {
      return NAME;
   }

   @Override
   public FunctionImport getFunctionImport()
   {
      // Returns the Result Set of the given query as String.
      ReturnType rt = new ReturnType()
            .setMultiplicity(EdmMultiplicity.ZERO_TO_ONE)
            .setTypeName(EdmSimpleTypeKind.String.getFullQualifiedName());

      // One required param: the SPARQL query.
      List<FunctionImportParameter> params = new ArrayList<>();
      params.add(new FunctionImportParameter()
            .setName("query")
            .setType(EdmSimpleTypeKind.String)
            .setFacets(new Facets().setNullable(false)));

      return new FunctionImport()
            .setName(NAME)
            .setHttpMethod("GET")
            .setParameters(params)
            .setReturnType(rt);
   }

   @Override
   public Object execute(Map<String, EdmLiteral> parameters) throws ODataException
   {
      EdmLiteral query_lit = parameters.remove("query");
      // Olingo2 checks for presence of non-nullable parameters for us!
      String query_s = query_lit.getLiteral();
      Query query = QueryFactory.create(query_s);
      if (!(query.isSelectType() || query.isDescribeType()))
      {
         throw new InvalidOperationException(query.getQueryType());
      }

      DrbCortexModel cortexmodel;
      try
      {
         cortexmodel = DrbCortexModel.getDefaultModel();
      }
      catch (IOException ex)
      {
         throw new RuntimeException(ex);
      }

      Model model = cortexmodel.getCortexModel().getOntModel();

      QueryExecution qexec = null;
      // FIXME: QueryExecution in newer versions of Jena (post apache incubation) implement AutoClosable.
      try
      {
         qexec = QueryExecutionFactory.create(query, model);
         if (query.isSelectType())
         {
            ResultSet results = qexec.execSelect();
            return ResultSetFormatter.asXMLString(results);
         }
         else
         {
            Model description = qexec.execDescribe();
            // newer version of Jena have the RIOT package for I/O
            StringWriter strwrt = new StringWriter();
            Abbreviated abb = new Abbreviated();
            abb.write(description, strwrt, null);
            return strwrt.toString();
         }
      }
      finally
      {
         if (qexec != null)
         {
            qexec.close();
         }
      }
   }

   public static class InvalidOperationException extends ExpectedException
   {
      public InvalidOperationException(int qtype)
      {
         super("Invalid operation " + queryTypeToString(qtype));
      }

      private static String queryTypeToString(int qtype)
      {
         switch (qtype)
         {
            case Query.QueryTypeAsk:       return "ASK";
            case Query.QueryTypeConstruct: return "CONSTRUCT";
            case Query.QueryTypeDescribe:  return "DESCRIBE";
            case Query.QueryTypeSelect:    return "SELECT";
            case Query.QueryTypeUnknown:
            default:
               return "unknown";
         }
      }
   }
}
