<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="java.util.List" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.util.StringList" %>
<%@ page import="com.matrixone.apps.domain.util.MapList" %>
<%@ page import="com.matrixone.apps.domain.DomainConstants" %>
<%@ page import="com.matrixone.apps.domain.util.FrameworkException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.matrixone.apps.domain.util.CASAuthentication" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Insert title here</title>
    <style type="text/css">
        table {
            font-family: "Lucida Sans Unicode", "Lucida Grande", Sans-Serif;
            border-collapse: collapse;
            /*color: #686461;*/
            color: #0d2b3f;
        }

        caption {
            padding: 10px;
            color: white;
            background: #8FD4C1;
            font-size: 18px;
            text-align: left;
            font-weight: bold;
        }

        th {
            border-bottom: 3px solid #5394a8;
            padding: 10px;
            text-align: left;
        }

        td {
            padding: 10px;
        }

        tr:nth-child(odd) {
            background: white;
        }

        tr:nth-child(even) {
            background: #E8E6D1;
        }

        a {
            font-size: 24px
        }
    </style>
</head>
<body>
<%
    CASAuthentication casAuthentication = new CASAuthentication("https://nn-3dexpdemo.niaepnn.ru/3dpassport");
    String authenticate =  casAuthentication.authenticate("admin_platform","Otujdhtg3");

    Context context = Framework.getContext(session);
    /**
     *object_id or user_name
     */
    String objectId = request.getParameter("objectId");

    int isPerson = 0;
    String assignment = "null";
    List<String> roles = new ArrayList<>();
    if (!objectId.contains(".")) {
        isPerson = 1;
        try {
            assignment = MqlUtil.mqlCommand(context, "print person $1 select assignment dump ;", objectId);
            roles = Arrays.asList(assignment.split(","));
            String tempString = MqlUtil.mqlCommand(context, "temp query bus Person $1 $2 select $3 dump", objectId, "-", "id");
            objectId = tempString.substring(tempString.lastIndexOf(",") + 1);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
    }

    String type = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "type");
    String name = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "name");
    String revision = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "revision");
    String spf_rev = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "attribute[IMS_SPFMajorRevision]");
    String spf_ver = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, "attribute[IMS_SPFDocVersion]");

    StringList selects = new StringList(DomainConstants.SELECT_ID);
    selects.addElement(DomainConstants.SELECT_TYPE);
    selects.addElement(DomainConstants.SELECT_NAME);
    selects.addElement(DomainConstants.SELECT_REVISION);

    StringList relSelects = new StringList(selects);
    relSelects.addElement(DomainConstants.SELECT_FROM_ID);
    relSelects.addElement(DomainConstants.SELECT_TO_ID);

    MapList relatedObjects = new MapList();
    MapList relations = new MapList();
    try {
        relatedObjects = new DomainObject(objectId)
                .getRelatedObjects(context,
                        /*relationship*/"*",
                        /*type*/"*",
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/ true, /*getFrom*/ true,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/ 0);
        relations = new DomainObject(objectId)
                .getRelatedObjects(context,
                        /*relationship*/"*",
                        /*type*/"*",
                        /*object attributes*/ null,
                        /*relationship selects*/ relSelects,
                        /*getTo*/ true, /*getFrom*/ true,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/ 0);
    } catch (Exception e) {

    }

    for (int i = 0; i < relatedObjects.size(); i++) {
        Map objectMap = (Map) relatedObjects.get(i);
        Map relationMap = (Map) relations.get(i);
        objectMap.put("fromId", relationMap.get(DomainConstants.SELECT_FROM_ID));
        objectMap.put("toId", relationMap.get(DomainConstants.SELECT_TO_ID));
        objectMap.put("relId", relationMap.get(DomainConstants.SELECT_ID));

    }

    pageContext.setAttribute("variables", relatedObjects);
    if (roles.size() > 0) {
        pageContext.setAttribute("roles", roles);
    }
    int counter = 1;
%>

<table>
    <tr>
        <th>Атрибут</th>
        <th>Значение</th>
    </tr>
    <tr>
        <td>Auth</td>
        <td><%=authenticate%>
        </td>
    </tr>
    <tr>
        <td>Контекст</td>
        <td><%=context.getUser()%>
        </td>
    </tr>
    <tr>
        <td>Тип</td>
        <td><%=type%>
        </td>
    </tr>
    <tr>
        <td>Код</td>
        <td><%=name%>
        </td>
    </tr>
    <tr>
        <td>Ревизия</td>
        <td><%=revision%>
        </td>
    </tr>
    <tr>
        <td>SPF</td>
        <td><%=spf_rev%>_<%=spf_ver%>
        </td>
    </tr>
    <tr>

    </tr>
</table>
<br>
<table>
    <tr>
        <th colspan="6">Все связанные объекты</th>
    </tr>
    <tr>
        <th>№ п/п</th>
        <th>Id</th>
        <th>Type</th>
        <th>Name</th>
        <th>Revision</th>
        <th>Rel.Id</th>
    </tr>
    <c:forEach var="var" items="${pageScope.variables}">
        <tr>
            <td><%=counter++%>
            </td>
            <td>
                <c:out value="${var.id}"/>
            </td>
            <td>
                <c:out value="${var.type}"/>
            </td>
            <td>
                <c:out value="${var.name}"/>
            </td>
            <td>
                <c:out value="${var.revision}"/>
            </td>
            <td>
                <li><c:out value="${var.relationship}"/></li>
                <li><c:out value="${var.relId}"/></li>
            </td>
        </tr>
    </c:forEach>
</table>
<c:if test="${roles.size()>0}">
    <table>
        <tr>
            <th>Список ролей</th>
        </tr>

        <c:forEach var="var" items="${pageScope.roles}">
            <tr>
                <td><c:out value="${var}"/></td>
            </tr>
        </c:forEach>

    </table>
</c:if>
<p>
    <a href="emxTree.jsp?objectId=<%=objectId%>" target="_blank">Переход</a>
</p>
</body>
</html>
