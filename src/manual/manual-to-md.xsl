<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  ~ Copyright (c) 2013 KloudTek Ltd
  -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://docbook.org/ns/docbook http://docbook.org/xml/5.0/xsd/docbook.xsd">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:output method="text" omit-xml-declaration="yes" indent="no" name="markdown"/>

    <xsl:template match="/">
        <xsl:apply-templates select="db:book/db:chapter"/>
        <xsl:value-of select="/db:title"/>
    </xsl:template>

    <xsl:template match="db:chapter">
        <xsl:variable name="filename" select="replace(db:title/text(),' ','-')"/>
        <xsl:result-document method="text" href="{$filename}.md">#
            <xsl:value-of select="db:title"/>

            <xsl:apply-templates select="db:para"/>
        </xsl:result-document>
    </xsl:template>

    <xsl:template match="db:para">
        <xsl:value-of select="."/>

    </xsl:template>
</xsl:stylesheet>

