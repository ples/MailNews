package org.mailnews.helper;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLHelper
{
    private int fontSize;
    private int spacing;
    private int fontHeaderSize;
    private int headerSpacing;
    private Font headerFont;
    private Font contentFont;
    private FontMetrics fm;
    private int marginText;
    private int marginImg;
    private Document document;
    private String path;
    private int textIndent;
    private int divWidth;

    private int divHeight;
    private int commonWidth = 0;

    public static final String STR_LEN_ATTR = "string-length";

    private double fontFault = 1.0;

    public HTMLHelper(int fontSize, int spacing, int fontHeaderSize, int headerSpacing, String headerFont,
            String fontName, int marginText, int marginImg, String path, int textIndent)
    {
        super();
        this.fontSize = fontSize;
        this.spacing = spacing;
        this.fontHeaderSize = fontHeaderSize;
        this.headerSpacing = headerSpacing;
        this.marginImg = marginImg;
        this.path = path;
        this.textIndent = textIndent;
        this.marginText = marginText;
        contentFont = new Font(fontName, 0, fontSize);
        this.headerFont = new Font(headerFont, Font.BOLD, fontHeaderSize);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        fm = img.getGraphics().getFontMetrics(contentFont);
    }

    private int getWidth(String content)
    {
        int width = (int) (fontFault * fm.stringWidth(content));
        commonWidth += width;
        return width;
    }

    private int getStringHeight(String content, int[][] maxWidth, int indent)
    {
        int height = 0;
        int currentWidth = indent;
        int stringCount = 1;
        String[] words = content.split(" ");
        int currentMaxWithIndex = 0;
        int spaceWidth = getWidth(" ");
        int currentMaxWidth = maxWidth[currentMaxWithIndex][1];
        for (int i = 0; i < words.length; i++)
        {
            currentWidth += (getWidth(words[i]) + spaceWidth);
            if (currentWidth - getWidth(" ") >= currentMaxWidth)
            {
                stringCount++;
                currentMaxWidth = maxWidth[currentMaxWithIndex][1];
                height = stringCount * fontSize + (stringCount - 1) * (spacing - fontSize) + (int) (0.2 * fontSize);
                if (height > maxWidth[currentMaxWithIndex][0])
                {
                    currentMaxWithIndex++;
                }
                currentWidth = 0;
                currentWidth += (getWidth(words[i]) + spaceWidth);
            }
        }
        height = stringCount * fontSize + (stringCount - 1) * (spacing - fontSize) + (int) (0.27 * fontSize);
        return height;
    }

    private int[] getImageSize(String filename)
    {
        File file = new File(path + filename);
        BufferedImage img;
        try
        {
            img = ImageIO.read(file);
            int[] size = new int[2];
            size[0] = img.getWidth();
            size[1] = img.getHeight();
            return size;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private int[] getNormalizedImageSize(int[] size)
    {
    	if(size==null)
    	{
    		size = new int[2];
    	}
        double ratio = ((double) size[0]) / size[1];
    	
        int[] ret = new int[2];
        if (size[0] > divWidth / 2 || size[1] > divHeight / 2)
        {
            if (size[0] > divWidth / 2)
            {
                ret[0] = divWidth / 2;
                ret[1] = (int) (ret[0] / ratio);

            }
            if (size[1] > divHeight / 2)
            {
                ret[1] = divHeight / 2;
                ret[0] = (int) (ret[1] * ratio);
            }
            return ret;
        }
        return size;
    }

    private Element getImgElem(String filename, String imgFloat, int[] size)
    {
        Element img = document.createElement("img");
        img.attr("src", filename);
        String style =
                (imgFloat != null ? "float: " + imgFloat + ";" : "") + "height: " + size[1] + "px; width: " + size[0]
                        + "px;";
        img.attr("style", style);
        return img;
    }

    private int[][] makeWidthsArray(int width, int height)
    {
        int[][] widths = new int[2][];
        widths[0] = new int[2];
        widths[1] = new int[2];
        widths[0][0] = height;
        widths[0][1] = divWidth - (width);
        widths[1][0] = divHeight;
        widths[1][1] = divWidth;
        return widths;
    }

    public String[] splitContent(String content, List<String> attachments, String type)
    {
        content = removeLotusAddings(content);
        document = Jsoup.parse(content);
        if (attachments == null)
            attachments = new ArrayList<String>();
        Elements p_tags = new Elements();
        
        removeImgTags(path,attachments);
        if ("plain".equals(type))
        {
            parsePlainText(p_tags, content);
        }
        else
        {
            parseHtmlText(p_tags);
        }
        
        Elements divs = new Elements();
        
        int attachmentCount = 0;
        int pCount = 0;
        Element div = document.createElement("div");
        boolean imgFloat = true;
        int height = marginText;
        while (attachmentCount < attachments.size() || pCount < p_tags.size())
        {
            if (attachments.size() > attachmentCount)
            {
                String att = attachments.get(attachmentCount);
                int[] size = getNormalizedImageSize(getImageSize(att));
                Element img = getImgElem(att, imgFloat ? "left" : "right", size);
                size[0] += 2 * marginImg;
                size[1] += 2 * marginImg;
                int imgHeight = size[1];
                int imgWidth = size[0];
                imgFloat = !imgFloat;

                if (size[1] + height < divHeight)
                    size[1] += height;
                else
                {
                    div = saveDiv(divs, div);
                    height = pCount < p_tags.size() ? marginText : 0;
                    continue;
                }

                if (pCount >= p_tags.size())
                {
                    int imgAdded = addImages(div, height, attachments, attachmentCount);
                    if (imgAdded > 0)
                    {
                        attachmentCount += imgAdded;
                    }
                    div = saveDiv(divs, div);
                    height = 0;
                    continue;
                }
                else
                {
                    div.appendChild(img);
                }
                while (pCount < p_tags.size())
                {
                    int strHeight = 0;
                    if ("p".equals(p_tags.get(pCount).tagName()))
                    {
                        String pText = p_tags.get(pCount).text();
                        strHeight = getStringHeight(pText, makeWidthsArray(imgWidth, imgHeight), 0);
                    }
                    else if ("table".equals(p_tags.get(pCount).tagName()))
                    {
                        strHeight = appendTable(p_tags, pCount, height, makeWidthsArray(imgWidth, imgHeight));
                    }
                    else
                    {
                        strHeight = getListHeight(p_tags.get(pCount), makeWidthsArray(imgWidth, imgHeight));
                    }
                    if ((height + strHeight) >= divHeight)
                    {
                        div = saveDiv(divs, div);
                        height = 0;
                        break;
                    }
                    else
                    {
                        height += strHeight + marginText;
                        div.appendChild(p_tags.get(pCount));
                        pCount++;
                    }
                    if (size[1] < height)
                    {
                        if (pCount >= p_tags.size())
                            div = saveDiv(divs, div);
                        break;
                    }
                    else
                    {
                        imgHeight -= (strHeight - marginText);
                        if (pCount >= p_tags.size())
                        {
                            height = divHeight;
                        }
                    }
                }
                attachmentCount++;
            }
            else
            {
                int strHeight = 0;
                int[][] widths = makeWidthsArray(0, 0);
                if ("p".equals(p_tags.get(pCount).tagName()))
                {
                    String pText = p_tags.get(pCount).text();
                    strHeight = getStringHeight(pText, widths, 0);
                }
                else if ("table".equals(p_tags.get(pCount).tagName()))
                {
                    strHeight = appendTable(p_tags, pCount, height, widths);
                }
                else
                {
                    strHeight = getListHeight(p_tags.get(pCount), widths);
                }
                if ((height + strHeight) >= divHeight)
                {
                    div = saveDiv(divs, div);
                    height = marginText;
                    continue;
                }
                else
                {
                    height += strHeight + marginText;
                    div.appendChild(p_tags.get(pCount));
                }
                pCount++;
                if (pCount >= p_tags.size())
                {
                    div = saveDiv(divs, div);
                    height = 0;
                }
            }
        } // END MAIN WHILE

        if (div.children().size() > 0)
        {
            saveDiv(divs, div);
        }
        String[] retDivs = new String[divs.size()];
        for (int i = 0; i < divs.size(); i++)
        {
            retDivs[i] = divs.get(i).toString();
        }
        return retDivs;
    }

    private int appendTable(Elements p_tags, int pCount, int height, int[][] widths)
    {
        int strHeight = getTableHeight(p_tags.get(pCount), widths);
        if (strHeight > divHeight)
        {
            Elements splitTables = splitHtmlTable(p_tags.get(pCount), divHeight - height, widths);
            if (splitTables == null)
                height = strHeight;
            else
            {
                p_tags.remove(pCount);
                p_tags.add(pCount, splitTables.first());
                p_tags.add(pCount + 1, splitTables.last());
                strHeight = getTableHeight(splitTables.first(), widths);
            }
        }
        return strHeight;
    }

    private Element saveDiv(Elements divs, Element div)
    {
        div.attr(STR_LEN_ATTR, String.valueOf(div.text().length() + 30 * div.getElementsByTag("img").size()));
        if (div.text().length() > 0 || div.getElementsByTag("img").size() > 0)
            divs.add(div);
        div = document.createElement("div");
        commonWidth = 0;
        return div;
    }

    private int addImages(Element div, int beginHeight, List<String> attachments, int beginAttachIndex)
    {
        Element imgContainer = document.createElement("div");
        imgContainer.attr("style", "text-align: center; font: initial;");
        imgContainer.attr("id", "photo-gallery");
        int imgDivHeight = 0;
        int imgDivWidth = 0;
        int maxHeight = 0;
        int imgAdded = 0;
        for (int i = beginAttachIndex; i < attachments.size(); i++)
        {
            String attachment = attachments.get(i);
            int[] size = getImgSizeForGalery(getImageSize(attachment));
            int absW = size[0] + 2 * marginImg;
            int absH = size[1] + 2 * marginImg;
            if (absW > (divWidth - imgDivWidth))
            {
                imgDivHeight += maxHeight;
                maxHeight = 0;
                imgDivWidth = 0;
            }
            if ((absH - marginImg) > (divHeight - (beginHeight + imgDivHeight)))
                break;
            if (absH > maxHeight)
                maxHeight = absH;

            imgContainer.appendChild(getImgElem(attachment, null, size));
            imgDivWidth += absW;
            imgAdded++;
        }
        if (imgAdded > 0)
        {
            div.appendChild(imgContainer);
        }
        return imgAdded;
    }

    private int[] getImgSizeForGalery(int[] size)
    {
    	if(size ==null)
    	{
    		size = new int[2];
    	}
        double ratio = ((double) size[0]) / size[1];
        int[] ret = new int[2];
        if (size[0] > divWidth * 0.9 || size[1] > divHeight *0.9)
        {
            if (size[0] > divWidth * 0.9)
            {
                ret[0] = (int)(divWidth * 0.9);
                ret[1] = (int) (ret[0] / ratio);

            }
            if (size[1] > divHeight / 2)
            {
                ret[1] = (int)(divHeight * 0.9);
                ret[0] = (int) (ret[1] * ratio);
            }
            return ret;
        }
        return size;
    }

    public int getHeaderHeight(String content, int[] margins_l_r_t_b, int indenter)
    {
        int contentSpacing = this.spacing;
        this.spacing = headerSpacing;
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        fm = img.getGraphics().getFontMetrics(headerFont);
        int height = 0;
        int[][] maxWidth = new int[2][];
        maxWidth[0] = new int[2];
        maxWidth[1] = new int[2];
        maxWidth[0][1] = divWidth - margins_l_r_t_b[0] - margins_l_r_t_b[1];
        maxWidth[1][1] = divWidth - margins_l_r_t_b[0] - margins_l_r_t_b[1];
        maxWidth[0][0] = divHeight;
        maxWidth[1][0] = divHeight;
        height = getStringHeight(content, maxWidth, indenter) + margins_l_r_t_b[2] + margins_l_r_t_b[3];
        this.spacing = contentSpacing;
        fm = img.getGraphics().getFontMetrics(contentFont);
        return height;
    }

    private void parseHtmlText(Elements p_tags)
    {
        ultParse(p_tags);
    }

    private void parsePlainText(Elements p_tags, String content)
    {
        content = content.replaceAll("\r\n\r\n", "\n");
        content = content.replaceAll("\r\n", "");
        String[] parags = content.split("\n");
        for (String p : parags)
        {
            p_tags.add(document.createElement("p").text(p));
        }
    }

    
    private int getListHeight(Element ul, int[][] maxWidths)
    {
        int height = 0;
        Elements lis = ul.children();
        maxWidths[0][1] -= marginText;
        maxWidths[1][1] -= marginText;
        for (Element li : lis)
        {
            if (height < maxWidths[0][0])
            {
                maxWidths[0][0] -= height;
            }
            else
            {
                maxWidths[0][0] = maxWidths[1][0];
                maxWidths[0][1] = maxWidths[1][1];
            }
            height += getStringHeight(li.text(), maxWidths, 0);
        }
        return height;
    }

    private int getTableHeight(Element table, int[][] maxWidths)
    {
        int height = 0;
        if (table.getElementsByTag("caption") != null && table.getElementsByTag("caption").size() > 0)
        {
            height += getStringHeight(table.getElementsByTag("caption").get(0).ownText(), maxWidths, 0);
        }
        int[] tdWidths = setTableWidth(table, maxWidths);
        Elements trs = table.getElementsByTag("tbody").get(0).children();
        for (int i = 0; i < trs.size(); i++)
        {
            Element tr = trs.get(i);
            int maxTdHeight = 0;
            for (int j = 0; j < tr.children().size(); j++)
            {
                int tdHeight =
                        getStringHeight(tr.children().get(j).ownText(),
                                makeWidthsArray(divWidth - tdWidths[j], divHeight), 0);
                if (tdHeight > maxTdHeight)
                    maxTdHeight = tdHeight;
            }
            height += maxTdHeight;
        }
        return height;
    }

    private int[] setTableWidth(Element table, int[][] maxWidths)
    {
        Elements trs = table.getElementsByTag("tbody").get(0).children();
        int columnsCount = 0;
        int rowCount = trs.size();
        Element maxTdCountTr = null;
        int[][] widths = new int[rowCount][];
        for (int i = 0; i < trs.size(); i++)
        {
            Element tr = trs.get(i);
            widths[i] = new int[tr.children().size()];
            if (widths[i].length > columnsCount)
            {
                maxTdCountTr = tr;
                columnsCount = widths[i].length;
            }
            for (int j = 0; j < tr.children().size(); j++)
            {
                widths[i][j] = getWidth(tr.children().get(j).ownText());
            }
        }
        int[] maxTdWidths = new int[columnsCount];
        for (int i = 0; i < columnsCount; i++)
        {
            int max = 0;
            for (int j = 0; j < rowCount; j++)
            {
                if (i < widths[j].length)
                {
                    if (widths[j][i] > max)
                        max = widths[j][i];
                }
            }
            maxTdWidths[i] = max;
        }
        int summ = 0;
        for (int i = 0; i < maxTdWidths.length; i++)
        {
            summ += maxTdWidths[i];
        }
        int tableWidth = maxWidths[0][1];
        table.attr("width", String.valueOf(tableWidth));
        int[] tdWidths = new int[maxTdWidths.length];
        for (int i = 0; i < maxTdCountTr.children().size(); i++)
        {
            tdWidths[i] = maxTdWidths[i] * tableWidth / summ;
            maxTdCountTr.children().get(i).attr("width", String.valueOf(tdWidths[i]));
        }
        return tdWidths;
    }

    public double getFontFault()
    {
        return fontFault;
    }

    public void setFontFault(double fontFault)
    {
        this.fontFault = fontFault;
    }

    public int getDivHeight()
    {
        return divHeight;
    }

    public void setDivHeight(int divHeight)
    {
        this.divHeight = divHeight;
    }

    public int getDivWidth()
    {
        return divWidth;
    }

    public void setDivWidth(int divWidth)
    {
        this.divWidth = divWidth;
    }

    public int getCommonWidth()
    {
        return commonWidth;
    }

    public void setCommonWidth(int commonWidth)
    {
        this.commonWidth = commonWidth;
    }

    private void ultParse(Elements ps)
    {

        Elements el = document.getAllElements();
        for (Element e : el)
        {
            Attributes at = e.attributes();
            for (Attribute a : at)
            {
                e.removeAttr(a.getKey());
            }
        }

        String regexLN = "(<br />|<p>|</p>|<div>|</div>)";
        String regexLI = "(<ul>|</ul>|<li>|</li>)";
        String regexTable = "(<table>|</table>)";
        String regexCaption = "(<caption>|</caption>)";
        String regexTR = "(<tr>|</tr>)";
        String regexTD = "(<td>|</td>|<th>|</th>)";
        String html = document.html();
        if (html.contains("<table>"))
        {
            Elements tabs = document.getElementsByTag("table");
            for (Element tab : tabs)
            {
                Elements tds = tab.getElementsByTag("td");
                tds.addAll(tab.getElementsByTag("th"));
                for (Element td : tds)
                {
                    td.html(td.text());
                }
            }
            html = document.html();
            html = html.replaceAll(regexTable, "#table#");
            html = html.replaceAll(regexCaption, "#caption#");
            html = html.replaceAll("(<tbody>|</tbody>)", "");
            html = html.replaceAll(regexTR, "#tr#");
            html = html.replaceAll(regexTD, "#td#");
            document.html(html);
        }
        if (html.contains("<ul>"))
        {
            Elements uls = document.getElementsByTag("ul");
            for (Element ul : uls)
            {
                Elements lis = ul.getElementsByTag("li");
                for (Element li : lis)
                {
                    li.html(li.text());
                }
            }
            html = document.html();
            html = html.replaceAll(regexLI, "#li#");
            document.html(html);
        }
        html = document.html().replaceAll(regexLN, "#n#");
        document.html(html);
        String[] texts = document.text().split("#n#");
        for (int i = 0; i < texts.length; i++)
        {
            if (texts[i].contains("#li#"))
            {
                ps.add(createHtmlList(texts[i]));
            }
            else if (texts[i].contains("#table#"))
            {
                ps.add(createHtmlTable(texts[i]));
            }
            else if (replaceNbsps(texts[i]).length() > 0)
            {
                ps.add(document.createElement("p").text(texts[i]));
            }
        }
    }
    
    private String replaceNbsps(String replacement)
    {
        String ret = replacement.replaceAll(" ", "").replaceAll("\u00a0", "");
        return ret;
    }

    private Element createHtmlList(String content)
    {
        Element ul = document.createElement("ul");
        String[] lis = content.split("#li#");
        for (int i = 0; i < lis.length; i++)
        {
            if (lis[i].replaceAll(" ", "").length() > 0)
            {
                ul.appendChild(document.createElement("li").text(lis[i]));
            }
        }
        return ul;
    }

    private Element createHtmlTable(String content)
    {
        Element table = document.createElement("table");

        content = content.replaceAll("#table#", "");
        if (content.contains("#caption#"))
        {
            table.appendChild(document.createElement("caption").text(content.split("#caption#")[1]));
        }
        Element tab = document.createElement("tbody");
        table.appendChild(tab);
        String[] trs = content.split("#tr#");
        for (int i = 0; i < trs.length; i++)
        {
            if (trs[i].replaceAll(" ", "").length() > 0)
            {
                Element tr = document.createElement("tr");
                String[] tds = trs[i].split("#td#");
                for (int j = 0; j < tds.length; j++)
                {
                    if (tds[j].replaceAll(" ", "").length() > 0)
                    {
                        tr.appendChild(document.createElement("td").text(tds[j]));
                    }
                }
                tab.appendChild(tr);
            }
        }
        return table;
    }

    private Elements splitHtmlTable(Element table, int lastHeight, int[][] widths)
    {
        Elements trs;
        Element fTable = document.createElement("table").html(table.html());
        trs = fTable.getElementsByTag("tr").remove();
        Element sTable = document.createElement("table").html(fTable.html());
        Element fTbody = fTable.getElementsByTag("tbody").first();
        Element sTbody = sTable.getElementsByTag("tbody").first();
        Elements tables;
        int trsLen = trs.size();
        for (int i = 0; i < trsLen; i++)
        {
            fTbody.appendChild(trs.remove(0));
            int currentHeight = getTableHeight(fTable, widths);
            if (currentHeight < lastHeight)
            {

            }
            else
            {
                if (i > 1)
                {
                    tables = new Elements();
                    trs.add(fTbody.children().last().clone());
                    fTbody.children().last().remove();
                    sTbody.html(trs.outerHtml());
                    tables.add(fTable);
                    tables.add(sTable);
                    return tables;
                }
                else
                {
                    break;
                }
            }
        }
        return null;
    }
    
    private void removeImgTags(String path,List<String> attachments)
    {
        Elements imgs = document.getElementsByTag("img").remove();
        // try to save images and append to attaches
        for(Element img : imgs)
        {
            try
            {
                URL url = null;
                try
                {
                    url = new URL(img.attr("src"));
                }
                catch (Exception e)
                {
                    url = new URL(img.attr("mce_real_src"));
                }
                BufferedImage image = ImageIO.read(url);
                String filename = url.toString().split("/")[url.toString().split("/").length-1];
                File imgFile = new File(path + filename);
                String format = filename.split("\\.")[1];
                format = "jpg".equals(format.toLowerCase())?"jpeg":format;
                ImageIO.write(image, format, imgFile);
                attachments.add(filename);
            }
            catch (Exception e1)
            {
                
            }
        }   
    }

    private String removeLotusAddings(String content)
    {
        String pattern = "(\\(See attached file:.*?\\))";
        content = content.replaceAll(pattern, "");
        return content;
    }
}