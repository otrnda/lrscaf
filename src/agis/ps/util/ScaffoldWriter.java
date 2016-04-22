/*
*File: agis.ps.util.ScaffoldWriter.java
*User: mqin
*Email: mqin@ymail.com
*Date: 2016年2月28日
*/
package agis.ps.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.biojava.nbio.core.sequence.DNASequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agis.ps.Path;
import agis.ps.link.Contig;
import agis.ps.path.Node;
import agis.ps.path.NodePath;

public class ScaffoldWriter {

	public static Logger logger = LoggerFactory.getLogger(ScaffoldWriter.class);
	private List<NodePath> paths;
	// private Map<String, DNASequence> cnts;
	private Map<String, Contig> cnts;
	private String filePath;

	// public ScaffoldWriter(List<NodePath> paths, Map<String, DNASequence>
	// cnts, String filePath) {
	// // TODO Auto-generated constructor stub
	// this.paths = paths;
	// this.cnts = cnts;
	// this.filePath = filePath;
	// }

	public ScaffoldWriter(List<NodePath> paths, Map<String, Contig> cnts, String filePath) {
		// TODO Auto-generated constructor stub
		this.paths = paths;
		this.cnts = cnts;
		this.filePath = filePath;
	}

	public void write() {
		// TODO Auto-generated method stub
		if (filePath == null)
			return;
		File out = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			out = new File(filePath);
			if (out.exists()) {
				logger.debug("The output file of scaffold is exist! It will not be overwrited!");
				logger.info("The output file of scaffold is exist! It will not be overwrited!");
				return;
			}
			if(!out.createNewFile())
			{
				logger.debug("ScaffoldWriter: The output file of scaffolds could not create!");
				logger.info("ScaffoldWriter: The output file of scaffolds could not create!");
				return;
			}
			fw = new FileWriter(out);
			bw = new BufferedWriter(fw);
			int count = 0; // scaffolds number;
			int sLen = 0; // scaffolds length;
			StringBuilder sb = null; 
//			boolean isAdded = false;
			boolean isNextUsed = false;
			Node node = null;
			String id = null;
			String seq = null;
			Node nNode = null;
			String nId = null;
			String nSeq = null;
			for (NodePath p : paths)
			{
//				System.out.println(count + " path");
				sb = new StringBuilder();
				bw.write(">scaffolds_" + count);
				bw.newLine();
				if(count == 28)
					System.out.println(">scaffolds_" + count);
				// if the path size equal to 1, 
				if(p.getPathSize() == 1)
				{
					node = p.getElement(0);
					id = node.getCnt().getID();
					if(count == 28)
						System.out.println("node id " + id);
					seq = "";
					if(node.getStrand().equals(Strand.FORWARD))
						seq = cnts.get(id).getSequenceAsString();
					else
						seq = cnts.get(id).getReverseComplementSeq();
					bw.write(seq);
					node = null;
					id = null;
					seq = null;
					count++;
					continue;
				}
				// else the path size large than 1;
				for(int i = 0 ; i < p.getPathSize(); i++)
				{
					if(i == p.getPathSize() - 1 && isNextUsed)
					{
						if(sb.length() != 0)
							bw.write(sb.toString());
						isNextUsed = false;
						sb = null;
						break;
					}
					if(i == p.getPathSize() - 1 && !isNextUsed)
					{
						node = p.getElement(i);
						id = node.getCnt().getID();
						seq = "";
						if(node.getStrand().equals(Strand.FORWARD))
							seq = cnts.get(id).getSequenceAsString();
						else
							seq = cnts.get(id).getReverseComplementSeq();
						bw.write(seq);
						node = null;
						id = null;
						seq = null;
						isNextUsed = false;
						break;
					}
					node = p.getElement(i);
					id = node.getCnt().getID();
					seq = "";
					if(node.getStrand().equals(Strand.FORWARD))
						seq = cnts.get(id).getSequenceAsString();
					else
						seq = cnts.get(id).getReverseComplementSeq();
					int nLen = node.getMeanDist2Next();
					int sdLen = node.getSdDist2Next();
					if(count == 28)
					{
						System.out.println("node id " + id);
						System.out.println("seq " + seq);
						System.out.println("len " + nLen);
						System.out.println("sd " + sdLen);
					}
					if(nLen < 0)
					{
						nNode = p.getElement(i + 1);
						nId = nNode.getCnt().getID();
						nSeq = "";
						if(nNode.getStrand().equals(Strand.FORWARD))
							nSeq = cnts.get(nId).getSequenceAsString();
						else
							nSeq = cnts.get(nId).getReverseComplementSeq();
						if(isNextUsed)
						{
							String temp = concatenate(sb.toString(), nSeq, nLen, sdLen);
							sb.append(temp);
							nNode = null;
							nId = null;
							nSeq = null;
							temp = null;
							isNextUsed = true;
						} else
						{
							String temp = concatenate(seq, nSeq, nLen, sdLen);
							sb.append(temp);
							nNode = null;
							nId = null;
							nSeq = null;
							temp = null;
							isNextUsed = true;
						}
					} else
					{
						if(sb.length() != 0)
						{
							bw.write(sb.toString());
							sb = null;
							sb = new StringBuilder();
						} else
						{
							bw.write(seq);
							seq = null;
						}
						// if not the last element, write the N
						if(i != p.getPathSize() - 1)
							bw.write(repeatString("N", nLen));
						isNextUsed = false;
					}
				}
				isNextUsed = false;
				bw.newLine();
				count++;
				sb = null;
				System.gc(); 
			}
//			for (NodePath p : paths) {
//				sb = new StringBuilder();
//				bw.write(">scaffolds_" + count + "\n");
//				for (int i = 0; i < p.getPathSize(); i++) {
//					if(i == p.getPathSize() - 1 && isAdded)
//						continue;
//					Node node = p.getElement(i);
//					String id = node.getCnt().getID();
//					String seq = "";
//					if(node.getStrand().equals(Strand.FORWARD))
//						seq = cnts.get(id).getSequenceAsString();
//					else 
//						seq = cnts.get(id).getReverseComplementSeq();
//					int nLen = node.getMeanDist2Next();
//					int sdLen = node.getSdDist2Next();
//					if(nLen < 0){
//						Node nNode = p.getElement(i + 1);
//						String nId = nNode.getCnt().getID();
//						String nSeq = "";
//						if(nNode.getStrand().equals(Strand.FORWARD))
//							nSeq = cnts.get(nId).getSequenceAsString();
//						else
//							nSeq = cnts.get(nId).getReverseComplementSeq();
//						if(isAdded){
//							String temp = concatenate(sb.toString(), nSeq, nLen, sdLen);
//							sb.append(temp);
//							isAdded = true;
//						} else {
//							String temp = concatenate(seq, nSeq, nLen, sdLen);
//							sb.append(temp);
//							isAdded = true;
//						}
//					} else {
//						if(!isAdded)
//							sb.append(seq);
//						if(i != p.getPathSize() - 1)
//							sb.append(repeatString("N", nLen));
//						isAdded = false;
//					}
////					if (i != p.getPathSize() - 1) {
////						if (nLen < 0){
////							int len = nLen + sdLen;
////							// get next element;
////							Node nNode = p.getElement(i + 1);
////							String nId = nNode.getCnt().getID();
////							String nSeq = "";
////							if(nNode.getStrand().equals(Strand.FORWARD))
////								nSeq = cnts.get(nId).getSequenceAsString();
////							else
////								nSeq = cnts.get(id).getReverseComplementSeq();
////							String cSeq = concatenate(seq, nSeq, nLen, sdLen);
////							bw.write(cSeq);
//////							bw.write(repeatString("M", nLen));
////						} else{
////							bw.write(seq);
////							bw.write(repeatString("N", nLen));
////						}
////					}
//				}
//				bw.write(sb.toString());
//				bw.write("\n");
//				isAdded = false;
//				count++;
//			}

		} catch (IllegalArgumentException e) {
			logger.debug(this.getClass().getName() + "\t" + e.getMessage() + "\t" + "IllegalArgumentException.");
			logger.error(this.getClass().getName() + "\t" + e.getMessage() + "\t" + "IllegalArgumentException.");
		} catch (IOException e) {
			logger.debug(this.getClass().getName() + "\t" + e.getMessage() + "\t" + "IOException.");
			logger.error(this.getClass().getName() + "\t" + e.getMessage() + "\t" + "IOException.");
		} catch (Exception e) {
			logger.debug(this.getClass().getName() + "\t" + e.getMessage() + "\t" + "Exception.");
			logger.error(this.getClass().getName() + "\t" + e.getMessage() + "\t" + "Exception.");
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					logger.debug(this.getClass().getName() + "\t" + e.getMessage());
					logger.error(this.getClass().getName() + "\t" + e.getMessage());
				}
		}

	}

	// if times larger than or equal to 0, used N indicated
	// else times less than 0, used M indicated;
	private String repeatString(String str, int times) {
		if (str == null)
			throw new IllegalArgumentException("ScaffoldWriter: The string could not be null!");
		// if(times < 0)
		// throw new IllegalArgumentException("ScaffoldWriter: The repeat times
		// could not be negative!");
		StringBuilder sb = new StringBuilder(str);
		if (times < 0) {
			times = 0 - times;
			for (int i = 0; i <= times; i++)
				sb.append(str);
		} else {
			for (int i = 1; i <= times; i++)
				sb.append(str);
		}
		return sb.toString();
	}
	
	private String concatenate(String seq1, String seq2, int len, int sd)
	{
		int range = Math.abs(len) + Math.abs(sd);
		String t1 = null;
		String t2 = null;
		if(seq1.length() <= range)
			t1 = seq1;
		else
			t1 = seq1.substring(seq1.length() - range);
		if(seq2.length() <= range)
			t2 = seq2;
		else 
			t2 = seq2.substring(0, range);
		Consensusser cs = new Consensusser();
		String value = cs.getConsensus(t1, t2, "nw");
		cs = null;
		StringBuilder sb = new StringBuilder();
		sb.append(seq1.substring(0, seq1.length() - range));
		sb.append(value);
		sb.append(seq2.substring(range));
		return sb.toString();
	}
}
