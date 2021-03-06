const Store = require('./storeModel')
const Book = require('./bookModel')

class BookRepository {
    constructor() {
    }

    addStore(newStore) {
        return Store.create(newStore)
        /*
         const store = new Store(newStore)
         return store.save()
         */
    }
    
    getStores() {
        return Store.find({})
    }

    async getStoresCount() {
        return await Store.count({})
    }

    getBookCategories() {
        return Book.distinct('category')
    }

    getBooks(category) {
        //Can be const query = Book.find() or you can specify the properties to return
        const query = Book.find({}, "_id isbn title authors publisher category pages reviews store")

        //If category is NOT undefined then addBook a where clause to the query
        if (category) {
            query.where({category: category})
        }

        //populate('store') will replace the store Id with the corresponding store object. (add , 'name'  to only get the store name)
        query.populate('store')
        return query
    }

    async getBooksCount() {
        return await Book.count({})
    }

    getBook(bookId) {
        //.select(...) is oprtional, only addBook it if you wish to specify the properties to be returned
        return Book.findById(bookId).select("_id isbn title authors publisher category pages reviews")
    }

    getBookByIsbn(isbn) {
        //I do not want to return the reviews and the __v (which is added automatically by MongoDB)
        return Book.findOne({isbn: isbn}).select('-reviews -__v')
    }

    addBook(newBook) {
        return Book.create(newBook)
        /*
         const book = new Book(newBook)
         return book.save()
         */
    }

    //More details about query operators @ https://docs.mongodb.org/manual/reference/operator/query/
    getBooksByAuthor(author) {
        return Book.find({authors: {$in: [author]}})
    }

    updateBook(bookId, updatedBook) {
        console.log('updateBook.updatedBook', updatedBook)
        delete updatedBook._id //Delete the _id if exists
        return Book.update({_id: bookId}, updatedBook)
        /* Book.find({ category: 'Fun', pages : { $gt : 200 } })
        Book.find({}).sort('isbn').limit( 5 )
        Book.find({}).where({ category: 'Fun' }).or({ $lt : 100 })
        Book.find( { reviews : { $exists: true } } )*/


        /*
         const book = await getBook(bookId)
         book.title = updatedBook.title
         book.authors = updatedBook.authors
         book.category = updatedBook.category
         book.read = updatedBook.read
         return book.save()
        */
    }

    deleteBook(bookId) {
        return Book.findByIdAndRemove(bookId)
        //return Book.remove({_id : bookId})
        //return getBook(bookId).then(book => book.remove())
    }

    async addReview(bookId, review) {
        const book = await getBook(bookId)
        book.reviews.push(review)
        return book.save()
    }

    async updateReview(bookId, reviewId, updatedReview) {
        const book = await getBook(bookId)
        const review = book.reviews.id(reviewId)
        review.rating = updatedReview.rating
        review.reviewText = updatedReview.reviewText
        return book.save()
    }

    async emptyDB() { //in case needed during testing
        await Book.remove({})
        await Store.remove({})
    }

    async initDb() {
        try {
            //Uncomment to empty the database
            await this.emptyDB()
            //If the db is empty then init the db with data in json files
            const booksCount = await this.getBooksCount()
            console.log(`Books Count: ${booksCount}. Comment out emptyDB() to stop re-initializing the database`)
            if (booksCount == 0) {
                await this.loadDataFromJsonFiles()
            }
        }
        catch (err) {
            console.log(err)
        }
    }

    async loadDataFromJsonFiles() {
        const fs = require('fs-extra')

        const store1 = await this.addStore({name: 'Jarir Bookstore', city: 'Doha'})
        const store2 = await this.addStore({name: 'Jarir Bookstore', city: 'Dubai'})

        const books = await fs.readJson('data/books.json')
        console.log('Retrieved books from json file and added to MongoDB books Collection: ' + books.length)

        for (const book of books) {
            //Assign store1 to even and store2 to odd ISBNs
            book.store = book.isbn % 2 ? store1._id : store2._id
            await this.addBook(book)
        }
    }
}

module.exports = new BookRepository()