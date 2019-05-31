package ilo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.Splitter;

public class HostParser {

	public List<String> parseHosts(String hosts) {
		List<String> servers = Splitter.on(",").omitEmptyStrings().splitToList(hosts);
		ArrayList<String> results = new ArrayList<String>();
		for (String server : servers) {
			results.addAll(parseHost(server));
		}
		return results;
	}

	public List<String> parseHost(String host) {
		return reduce(expand(host));
	}

	List<List<String>> expand(String host) {
		List<List<String>> results = new ArrayList<>();
		List<String> parts = Splitter.on(".").splitToList(host);

		for (String part : parts) {
			results.add(expandInt(part));
		}
		return results;
	}

	public List<String> reduce(List<List<String>> parts) {
		ArrayDeque<List<String>> stack = new ArrayDeque<List<String>>(parts);
		List<String> results = new ArrayList<String>();
		List<String> items = stack.pop();
		for (String item : items) {
			reduce(item, new ArrayDeque<List<String>>(stack), results);
		}
		return results;
	}

	public void reduce(String pre, ArrayDeque<List<String>> stack, List<String> results) {
		boolean isBottom = stack.isEmpty();
		if (isBottom) {
			results.add(pre);
			return;
		}

		while (!stack.isEmpty()) {
			List<String> items = stack.pop();
			for (String item : items) {
				String newPre = pre + "." + item;
				if (!isBottom) {
					reduce(newPre, stack, results);
				}
			}
		}
	}

	List<String> expandInt(String part) {
		List<String> range = Splitter.on("-").splitToList(part);
		if (range.size() == 1) {
			return range;
		}
		int start = Integer.parseInt(range.get(0));
		int end = Integer.parseInt(range.get(1));
		return IntStream.rangeClosed(start, end).mapToObj(i -> "" + i).collect(Collectors.toList());
	}
}
