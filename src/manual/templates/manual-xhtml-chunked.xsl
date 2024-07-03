<?xml version='1.0'?>
<!--
  ~ Copyright (c) 2024 Aeontronix Inc
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="../../../_build/docbook-xsl/xhtml5/chunk.xsl"/>
    <xsl:import href="manual-common.xsl"/>
    <xsl:param name="chunk.section.depth" select="0"/>
    <xsl:template name="user.head.content">
        <script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js" type="text/javascript"/>
    </xsl:template>
</xsl:stylesheet>
