This module is a series of tests that makes sure that request wrappers works as they should. Listed is a number of url's
that return results depending on whether the request wrappers are correct in different scenarios.

* ServletDispatcherFilter
This url tests that the overriding of servletPath and pathInfo works
http://localhost:8080/magnoliaAuthor/.magnolia/dispatcherfiltertest/servletdispatch?a=2

* UnicodeNormalizationFilter
This url tests that request parameters are not hidden by the UnicodeNormalizationFilter after a forward() is performed.
http://localhost:8080/magnoliaAuthor/.magnolia/normalizationfiltertest/dispatch

* MultipartRequestFilter
This url tests that processing of a multipart request works. That it doesn't hide parameters after a forward() is performed.
http://localhost:8080/magnoliaAuthor/.magnolia/multipartfiltertest/submit
For testing it, this url will display a form that submits to the above url.
http://localhost:8080/magnoliaAuthor/.magnolia/multipartfiltertest/form

* Page Rendering
This url tests that page rendering works as it should and that no request wrappers are lost on includes.
http://localhost:8080/magnoliaAuthor/sanity-test-page.html?a=2
(should be tested with magnolia.utf8.enabled set to true and to false)


Success is defined as:
	response status code is 200
	body text contains "TEST COMPLETED"
	body text does not contain the text "ERROR"

