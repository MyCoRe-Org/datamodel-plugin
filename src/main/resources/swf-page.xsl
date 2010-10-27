<?xml version="1.0" encoding="iso-8859-1"?>
  <!-- ============================================== -->
  <!-- $Revision: 2193 $ $Date: 2009-08-28 15:12:21 +0200 (Fr, 28 Aug 2009) $-->
  <!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" />
  <xsl:param name="objectType" />
  <xsl:param name="step" />
  <xsl:param name="titleuri" />
  <xsl:param name="layout" />
  <xsl:variable name="titles" select="document($titleuri)/titles" />

  <xsl:template name="getTitle">
    <xsl:param name="lang" />
    <xsl:choose>
      <xsl:when test="$titles/title[@step=$step and @type=$objectType and lang($lang)]">
        <xsl:value-of select="$titles/title[@step=$step and @type=$objectType and lang($lang)]" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat('Title(',$lang,'):',$step,'-',$objectType)" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@title[parent::section]" priority="2">
    <xsl:attribute name="{local-name()}">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="lang" select="parent::*/@xml:lang" />
      </xsl:call-template>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="editor/@id|components/@root">
    <xsl:attribute name="{local-name()}">
	   <xsl:choose>
	     <xsl:when test="$layout">
	       <xsl:value-of select="concat('edit-',$objectType,'-',$layout)" />
	     </xsl:when>
	     <xsl:otherwise>
	       <xsl:value-of select="concat('edit-',$objectType)" />
	     </xsl:otherwise>
	   </xsl:choose>
	 </xsl:attribute>
  </xsl:template>

  <xsl:template match="include/@uri">
    <xsl:attribute name="{local-name()}">
	   <xsl:choose>
	     <xsl:when test="$layout">
	       <xsl:value-of select="concat('webapp:editor/editor-',$objectType,'-',$layout,'.xml')" />
	     </xsl:when> 
	     <xsl:otherwise>
	       <xsl:value-of select="concat('webapp:editor/editor-',$objectType,'.xml')" />
	     </xsl:otherwise>
	   </xsl:choose>
	 </xsl:attribute>
  </xsl:template>

  <xsl:template match="target/@name">
    <xsl:attribute name="{local-name()}">
      <xsl:choose>
        <xsl:when test="$step='author'">
          <xsl:value-of select="'CreateObjectServlet'" />
        </xsl:when>
        <xsl:when test="$step='commit'">
          <xsl:value-of select="'UpdateObjectServlet'" />
        </xsl:when>
        <xsl:when test="$step='editor'">
          <xsl:value-of select="'MCRCheckEditDataServlet'" />
        </xsl:when>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>