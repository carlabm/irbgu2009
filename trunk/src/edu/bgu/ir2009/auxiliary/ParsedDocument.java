package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:02:24
 */
public class ParsedDocument {
    private final Map<String, Set<Long>> terms = new HashMap<String, Set<Long>>();
    private final String docNo;
    private final long date;
    private final String byLine;
    private final String cn;
    private final String in;
    private final String tp;
    private final String pub;
    private final String page;
    private final String text;

    public ParsedDocument(UnParsedDocument unParsedDoc) {
        docNo = unParsedDoc.getDocNo();
        date = unParsedDoc.getDate();
        byLine = unParsedDoc.getByLine();
        cn = unParsedDoc.getCn();
        in = unParsedDoc.getIn();
        tp = unParsedDoc.getTp();
        pub = unParsedDoc.getPub();
        page = unParsedDoc.getPage();
        text = unParsedDoc.getText();
    }

    public ParsedDocument(String serialized) {
        int start = 0;
        int end = serialized.indexOf('|');
        docNo = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        date = Long.parseLong(serialized.substring(start, end));
        start = end + 1;
        end = serialized.indexOf('|', start);
        byLine = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        cn = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        in = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        tp = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        pub = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        page = serialized.substring(start, end);
        start = end + 1;
        text = serialized.substring(start);
    }

    /* //FT933-870|total=0.05265579865312133,waterhous=0.05265579865312133,half=0.05265579865312133,|930927||G
    private void parseDocVector(String serialized, int start, int end) {
        while (start < end) {
            int currEnd = serialized.indexOf('=', start);
            String currTerm = serialized.substring(start, currEnd);
            start = currEnd + 1;
            currEnd = serialized.indexOf(',',start);
            Double currTermWeight = Double.parseDouble(serialized.substring(start, currEnd));
            documentVector.put(currTerm, currTermWeight);
            start = currEnd + 1;
        }
    }*/

    public void addTerm(String term, long pos) {
        Set<Long> posSet = terms.get(term);
        if (posSet == null) {
            posSet = new LinkedHashSet<Long>();
            terms.put(term, posSet);
        }
        posSet.add(pos);
    }

    public Map<String, Set<Long>> getTerms() {
        return Collections.unmodifiableMap(terms);
    }

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append(docNo.replace('|', ' ')).append('|')
                .append(date).append('|')
                .append(byLine.replace('|', ' ')).append('|')
                .append(cn.replace('|', ' ')).append('|')
                .append(in.replace('|', ' ')).append('|')
                .append(tp.replace('|', ' ')).append('|')
                .append(pub.replace('|', ' ')).append('|')
                .append(page.replace('|', ' ')).append('|')
                .append(text);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedDocument that = (ParsedDocument) o;

        if (!docNo.equals(that.docNo)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return docNo.hashCode();
    }

    public String getDocNo() {
        return docNo;
    }

    public long getDate() {
        return date;
    }

    public String getByLine() {
        return byLine;
    }

    public String getCn() {
        return cn;
    }

    public String getIn() {
        return in;
    }

    public String getTp() {
        return tp;
    }

    public String getPub() {
        return pub;
    }

    public String getPage() {
        return page;
    }

    public String getText() {
        return text;
    }

    public static void main(String[] args) {
        ParsedDocument parsedDocument = new ParsedDocument("FT933-679|unemploy=0.1625807708873452,understand=0.04645164882495577,|930928|By JOE ROGALY|GBZ  United Kingdom, EC.|P9311 Finance, Taxation, and Monetary Policy.|CMMT  Comment & Analysis.     ECON  Employment & unemployment.     ECON  Inflation.|The Financial Times|London Page 18| It is easy to mock promises of full employment. How is the promise to be kept? The short answer is, of course, by diverting the Treasury's strategic eye from its proper objective, which is to keep inflation low and preferably falling. Since the 1970s, when inflation in some OECD countries threatened to run away with itself, this answer has been a perfect debate-stopper. The assumption, rarely spoken, always understood, has been that stable prices are more important than the 'maintenance of a high and stable level of employment'. With acknowledgements to Mr Gordon Brown, I take the latter phrase from the wartime coalition government's 1944 white paper on employment policy. Mr Brown, you will recall, is Labour's shadow chancellor. We will return to him in a moment, but let us first pursue the argument a little further. Whatever brand of economic theory you buy, whatever your fiscal or monetary stance, whether you are a monetarist or a fine-tuner of demand, a devaluation freak or a mere exchange rate floater, at the end of the day you get stable prices by keeping output costs down. This can be done by increasing productivity, which except in times of rapid growth means sacking redundant workers. Smarter ways of producing goods or services, or better machinery, can give more production per head, but without a concomitant rise in sales you need fewer heads. In such circumstances those who keep their jobs are likely to settle for modest increases in pay. You ensure this by putting the fear of unemployment into the souls of existing and potential employees. In short, if it takes a certain level of unemployment to loosen up the labour market, so be it. If the terror of the dole queue is the only weapon that will blast away trade union dominance, then terror it has to be. I have eschewed the use of economists' phrases about competitive advantage and workers pricing themselves out of jobs. A great deal of such jargon is merely euphemistic. I have been hearing versions of these contemporary cliches since I was old enough to understand the words supply and demand. They mean very little to the chap whose pay packet shrinks or disappears. There is no getting away from the permanent political choice between job stability and price stability. It would be wrong, therefore, to castigate Mr Brown for restating Labour's commitment to full employment, as he did again yesterday at the very start of his address to the party's conference in Brighton. He thumped out his list of short-term plans for reducing unemployment with all the passion but little of the skill of a driven platform orator, hitting the rostrum and missing the mark. Yet we may forgive him his delivery. Given the choice, how could a party of the left say other than that unemployment is the number one priority? The positive elements in Mr Brown's speech should be taken seriously, as must the pamphlet, published under his name, that accompanied it. You can get the pamphlet by sending Pounds 1 to Mr Brown at the House of Commons, London SW1A OAA. Meanwhile we will leave aside crass considerations of what a promise of jobs for all may do for Labour's standing among the voters, or its author's chances of making it to leadership of his party. Let us, rather, join this over-earnest, too-eager-to-please politician in reasoned discourse. Mr Brown does not concede that the goal of full employment can only be pursued at the cost of rising inflation. 'Labour believes in keeping inflation low, and of course will stick to strict low inflation targets,' he writes. He is not dodging the issue; merely seeking to persuade us that we can grow our way out of the need to face the perennial trade-off. He cites the concept of a non-accelerating inflation rate of unemployment ('nairu') as a pet theory that its proponents, the Tories, do not properly understand. His approach is to go for growth by throwing in most macroeconomic nostrums except devaluation. He regards the latter as 'inappropriate and counter-productive'. He repeats his enthusiasm for the education and training of a highly skilled workforce and takes from the Clinton professors, Bradford de Long and Lawrence Summers, the view that countries that invest heavily in equipment reap a harvest of higher rates of growth of output per head. His pamphlet has been well received, particularly by those who came to it prepared for something less closely argued. Whether it makes sense or not is more a question of instinct than rational deliberation. Everything depends on whether you believe that a government in which Mr Brown was chancellor would foster a more rapid rate of growth than Britain has managed for any appreciable period since the war. Who knows? Perhaps when we have our highly-trained workforce we can become a new Japan. Yesterday Mr Brown's was the same message as the one delivered by the party leader, Mr John Smith, to the Trades Union Congress a few weeks ago. Non-inflationary growth can be sustained. Not everyone will believe it. In the run-up to last year's election Mr Smith sought to convince voters that growth would take care of any gap between Labour's proposed public expenditure and likely revenue from taxation. The Conservatives turned out to be more successful spinners of this yarn. In the Tory case it has proved fanciful; they promised everything and so far have delivered nothing - except low inflation. The tide of opinion is turning away from a huge cheer even at that. The chancellor, Mr Kenneth Clarke, is now talking about reducing unemployment as a matter of priority. Naturally he wants sustained growth and low inflation as well. Don't we all? Mr Clarke is a man who visibly chooses to have his cake and eat it too. The unemployment theme has recently been echoed at the IMF, in the OECD, and, with strongest emphasis but perhaps least effect, in the European Community. When he was shadow chancellor Mr Smith was not permitted by the then Labour leader, Mr Neil Kinnock, to preach full employment. It did not sound good in those hard monetarist times. Phrases like 'the highest possible level of employment' were used instead. Since he became leader Mr Smith has turned his party towards its traditional stance of openly aspiring to a full employment economy. He may catch a tide. ");
        int i = 0;
    }
}
