<?xml version='1.0'?>
<!--
  ~ Copyright (c) 2013 KloudTek Ltd
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="node()" mode="rows">
        <td nowrap="true">
            <table style="width:100%;border-right: thin solid;
                         border-top: thin solid; border-left:
                         thin solid; border-bottom: thin solid;">
                <xsl:apply-templates select="@*"/>
            </table>
        </td>
    </xsl:template>

    <xsl:template match="@*">
        <tr>
            <td style="font-size: larger; text-transform:
                      uppercase; background-color: gainsboro">
                <xsl:value-of select="name()"/>
            </td>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>