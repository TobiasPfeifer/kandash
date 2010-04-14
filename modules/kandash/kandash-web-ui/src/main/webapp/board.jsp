<%-- 
    Document   : board
    Created on : 10.04.2010, 1:21:05
    Author     : Администратор
--%>

<%@page contentType="text/html" pageEncoding="windows-1251"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <!-- The following line defines content type and utf-8 as character set. -->
        <!-- If you want your application to work flawlessly with various local -->
        <!-- characters, just make ALL strings, on the page, json and database utf-8. -->
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">

        <!-- Ext relies on its default css so include it here. -->
        <!-- This must come BEFORE javascript includes! -->
        <link rel="stylesheet" type="text/css" href="js/ext/resources/css/ext-all.css">
        <link rel="stylesheet" type="text/css" href="js/ext/resources/css/xtheme-access.css">

        <!-- Include here your own css files if you have them. -->

        <!-- First of javascript includes must be an adapter... -->
        <script type="text/javascript" src="js/ext/adapter/ext/ext-base.js"></script>

        <!-- ...then you need the Ext itself, either debug or production version. -->
        <script type="text/javascript" src="js/ext/ext-all.js"></script>

        <!-- Include here you application javascript file if you have it. -->

        <!-- Set a title for the page (id is not necessary). -->
        <title id="page-title">Kanban dashboard</title>

        <!-- You can have onReady function here or in your application file. -->
        <!-- If you have it in your application file delete the whole -->
        <!-- following script tag as we must have only one onReady. -->
        <script type="text/javascript" src="js/com/vasilrem/kandash/ProjectNamespace.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/Controller.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/BoardUtil.js"></script>
        <script type="text/javascript" src="js/ext/ext-basex.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/CreateTierDialog.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/CreateProjectDialog.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/CreateTaskDialog.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/Project.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/Tier.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/TaskForm.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/Task.js"></script>
        <script type="text/javascript" src="js/com/vasilrem/kandash/board/Board.js"></script>

        <script type="text/javascript" src="js/dashboard.js"></script>

        <!-- Close the head -->
    </head>
    <body>
        <script>              
            var boardFromRequest = '<%=request.getParameter("board")%>'
        </script>
    </body>
</html>
