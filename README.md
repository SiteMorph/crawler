crawler - inspecting crawler with basic features and small footprint 
=======

A basic inspection type crawler designed to be used as part of website audit
and investigation. Supports robots, crawl rate limits crawled pages limit, 
mime type limits and domain restrictions.

The spider is intended to be used to fetch html pages rather than being a 
general purpose crawler. Other projects provide more complete and functional
implementations. This package is a light weight implementation of basic 
inspection features for website audits.

The spider supports a basic two stage filtering approach to managing pages.

1. Filter on urls being considered to be crawled. This is a good time to strip
    out things like obviously.
2. Once a page has been fetched, should it be processed / stored.

Usage
-----

See tests for usage scenarios. The basic approach is to use the Spider.Builder
to construct a spider task which runs over the set parameters. There are 
a number of helper methods and classes that should be explored as needed.
