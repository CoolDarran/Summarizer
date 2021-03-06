<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html lang="zh-cn">
  <head>
    <title>新闻多文档摘要</title>
    
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Bootstrap -->
    <link rel="stylesheet" href="./css/bootstrap.min.css">
	<!-- Custom styles for this template -->
    <link href="./css/footer.css" rel="stylesheet">
    <link href="./css/custom.css" rel="stylesheet">
    
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="./js/base/html5shiv.min.js"></script>
        <script src="./js/base/respond.min.js"></script>
    <![endif]-->
  </head>
  
  
  <body>
  
  	<s:set name="sum" value = "sum" />
  	<s:set name="summary" value = "sum.summary" />
  	<s:set name="keyWords" value = "sum.summary" />
  	<s:set name="urls" value = "sum.urls" />
  
    <!-- Wrap all page content here -->
    <div id="wrap">

      <!-- Begin page content -->
      <div class="container">
        <div class="page-header">
          <h1>新闻多文档摘要</h1>
        </div>
        
		<form class="form-horizontal" role="form" action="searchWords">
		<s:if test="%{#sum == null}">
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		<div class="form-group"></div>
  		</s:if>
  		
  		<div class="form-group">
 			<div class="col-lg-12">
    			<div class="input-group">
    				<input type="text" class="form-control input-lg" name="keyWords" value="<s:property value="keyWords"/>">
      				<span class="input-group-btn">
        				<button type="submit" class="btn btn-primary btn-lg">Search!</button>
      				</span>      				     				
    			</div><!-- /input-group -->
    			<span class="help-block">目前摘要较慢，请耐心等候。</span>
 			</div><!-- /.col-lg-6 -->
		</div><!-- /.row -->
		
		<s:if test="%{#summary !=null}">
			<div class="form-group">
				<div class="col-lg-12">
					<h2>新闻摘要：</h2>
				</div>
				<div class="col-lg-12">
				<s:property value="summary" />
				</div>
			</div>
		</s:if>
		<s:if test="%{#urls !=null}">
			<div class="form-group">
				<div class="col-lg-12">
					<h2>新闻来源：</h2>
				</div>
				<div class="col-lg-12">
				
				<ol>
				<s:iterator value="urls" var="urlAndTitle" status="ut">
					<s:set name="uAndT" value='#urlAndTitle.split(",")'></s:set>
  						<li>
  							<h4>							
								<a href="<s:property value="#uAndT[1]"/>" target="_blank"><s:property value="#uAndT[0]"/></a>											
  							</h4>
  						</li>
				</s:iterator>
				</ol>
				</div>
			</div>
		</s:if>
		</form>
				
      </div>
    </div>

    <div id="footer">
      <div class="container">
        <p class="text-muted">Copyright © 2014 <a href="http://www.scutdm.com" target="_blank">SCUT Data Mining Research Group</a>. All Rights Reserved..</p>
      </div>
    </div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="./js/base/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="./js/base/bootstrap.min.js"></script>
  </body>
</html>